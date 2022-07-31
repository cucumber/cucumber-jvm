package io.cucumber.core.options;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import static java.net.Proxy.NO_PROXY;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

public final class CurlOption {

    private final Proxy proxy;
    private final URI uri;
    private final HttpMethod method;
    private final List<Entry<String, String>> headers;

    private CurlOption(Proxy proxy, HttpMethod method, URI uri, List<Entry<String, String>> headers) {
        this.proxy = requireNonNull(proxy);
        this.uri = requireNonNull(uri);
        this.method = requireNonNull(method);
        this.headers = requireNonNull(headers);
    }

    @SafeVarargs
    public static CurlOption create(HttpMethod method, URI uri, Entry<String, String>... headers) {
        return new CurlOption(NO_PROXY, method, uri, asList(headers));
    }

    public static CurlOption parse(String cmdLine) {
        List<String> args = ShellWords.parse(cmdLine);

        URI url = null;
        HttpMethod method = HttpMethod.PUT;
        List<Entry<String, String>> headers = new ArrayList<>();
        Proxy proxy = NO_PROXY;

        while (!args.isEmpty()) {
            String arg = args.remove(0);
            if (arg.equals("-X")) {
                String methodArg = removeArgFor(arg, args);
                method = HttpMethod.parse(methodArg);
            } else if (arg.equals("-H")) {
                String headerArg = removeArgFor(arg, args);
                SimpleEntry<String, String> e = parseHeader(headerArg);
                headers.add(e);
            } else if (arg.equals("-x")) {
                String proxyArg = removeArgFor(arg, args);
                proxy = parseProxy(proxyArg);
            } else {
                if (url != null) {
                    throw new IllegalArgumentException("'" + cmdLine + "' was not a valid curl command");
                }
                url = parseUrl(arg);
            }
        }

        if (url == null) {
            throw new IllegalArgumentException("'" + cmdLine + "' was not a valid curl command");
        }
        return new CurlOption(proxy, method, url, headers);
    }

    private static URI parseUrl(String arg) {
        try {
            return new URI(arg);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("'" + arg + "' was not a valid url", e);
        }
    }

    private static Proxy parseProxy(String arg) {
        URI url;
        try {
            url = new URI(arg);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("'" + arg + "' was not a valid proxy address", e);
        }

        String protocol = url.getScheme();
        if (protocol == null) {
            throw new IllegalArgumentException("'" + arg + "' did not have a valid proxy protocol");
        }

        Proxy.Type type;
        if (protocol.equalsIgnoreCase("http") || protocol.equalsIgnoreCase("https")) {
            type = Type.HTTP;
        } else if (protocol.equalsIgnoreCase("socks")) {
            type = Type.SOCKS;
        } else {
            throw new IllegalArgumentException("'" + arg + "' did not have a valid proxy protocol");
        }

        String host = url.getHost();
        if (host == null) {
            throw new IllegalArgumentException("'" + arg + "' did not have a valid proxy host");
        }

        int port = url.getPort();
        if (port == -1) {
            throw new IllegalArgumentException("'" + arg + "' did not have a valid proxy port");
        }

        return new Proxy(type, new InetSocketAddress(host, port));
    }

    private static String removeArgFor(String arg, List<String> args) {
        if (!args.isEmpty()) {
            return args.remove(0);
        }
        throw new IllegalArgumentException("Missing argument for " + arg);
    }

    private static SimpleEntry<String, String> parseHeader(String headerArg) {
        String[] parts = headerArg.split(":", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("'" + headerArg + "' was not a valid header");
        }
        return new SimpleEntry<>(parts[0].trim(), parts[1].trim());
    }

    public HttpMethod getMethod() {
        return method;
    }

    public List<Entry<String, String>> getHeaders() {
        return headers;
    }

    public URI getUri() {
        return uri;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public enum HttpMethod {
        GET,
        HEAD,
        POST,
        PUT,
        PATCH,
        DELETE,
        OPTIONS,
        TRACE;

        static HttpMethod parse(String argument) {
            for (HttpMethod value : HttpMethod.values()) {
                if (value.name().equals(argument)) {
                    return value;
                }
            }
            throw new IllegalArgumentException(argument + " was not a http method");
        }
    }

}
