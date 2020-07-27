package io.cucumber.core.options;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public final class CurlOption {

    private final URI uri;
    private final HttpMethod method;
    private final List<Entry<String, String>> headers;

    public CurlOption(URI uri, HttpMethod method, List<Entry<String, String>> headers) {
        this.uri = uri;
        this.method = method;
        this.headers = headers;
    }

    public static CurlOption parse(String cmdLine) {
        List<String> args = ShellWords.parse(cmdLine);

        String url = null;
        HttpMethod method = HttpMethod.PUT;
        List<Entry<String, String>> headers = new ArrayList<>();

        while (!args.isEmpty()) {
            String arg = args.remove(0);
            if (arg.equals("-X")) {
                String methodArg = removeArgFor(arg, args);
                method = HttpMethod.parse(methodArg);
            } else if (arg.equals("-H")) {
                String headerArg = removeArgFor(arg, args);
                SimpleEntry<String, String> e = parseHeader(headerArg);
                headers.add(e);
            } else {
                if (url != null) {
                    throw new IllegalArgumentException("'" + cmdLine + "' was not a valid curl command");
                }
                url = arg;
            }
        }

        if (url == null) {
            throw new IllegalArgumentException("'" + cmdLine + "' was not a valid curl command");
        }
        try {
            return new CurlOption(new URI(url), method, headers);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
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
