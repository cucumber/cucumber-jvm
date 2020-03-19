package io.cucumber.core.plugin;

import io.cucumber.core.options.CurlOption;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

class UrlOutputStream extends OutputStream {
    // Allow streaming, using a chunk size that is similar to a typical NDJSON message length
    public static final int CHUNK_LENGTH = 256;
    private final HttpURLConnection urlConnection;
    private final OutputStream outputStream;

    private String method;
    private URL url;
    private final Map<String, List<String>> requestHeaders;

    UrlOutputStream(CurlOption option) throws IOException {
        this.method = option.getMethod().name();
        this.url = option.getUri().toURL();

        urlConnection = (HttpURLConnection) this.url.openConnection();
        for (Entry<String, String> header : option.getHeaders()) {
            urlConnection.setRequestProperty(header.getKey(), header.getValue());
        }
        urlConnection.setRequestMethod(this.method);
        urlConnection.setDoOutput(true);
        urlConnection.setChunkedStreamingMode(CHUNK_LENGTH);
        urlConnection.setInstanceFollowRedirects(false);
        requestHeaders = urlConnection.getRequestProperties();
        outputStream = urlConnection.getOutputStream();
    }

    @Override
    public void write(byte[] buffer, int offset, int count) throws IOException {
        outputStream.write(buffer, offset, count);
    }

    @Override
    public void write(byte[] buffer) throws IOException {
        outputStream.write(buffer);
    }

    @Override
    public void write(int b) throws IOException {
        outputStream.write(b);
    }

    @Override
    public void flush() throws IOException {
        outputStream.flush();
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
        int httpStatus = urlConnection.getResponseCode();
        boolean redirect = httpStatus >= 300 && httpStatus < 400;
        boolean error = httpStatus >= 400;
        try (InputStream inputStream = error ? urlConnection.getErrorStream() : urlConnection.getInputStream()) {
            Map<String, List<String>> responseHeaders = urlConnection.getHeaderFields();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, UTF_8))) {
                String responseBody = br.lines().collect(Collectors.joining(System.lineSeparator()));
                if (error || redirect) {
                    String message = generateCurlLikeMessage(this.method, this.url, this.requestHeaders, responseHeaders, responseBody, redirect);
                    throw new IOException(message);
                }
            }
        }
    }

    static String generateCurlLikeMessage(
        String method,
        URL url,
        Map<String, List<String>> requestHeaders,
        Map<String, List<String>> responseHeaders,
        String responseBody,
        boolean redirect) {
        return String.format(
            "%s:\n> %s %s\n%s%s\n%s",
            redirect ? "HTTP redirect not supported" : "HTTP request failed",
            method,
            url,
            headersToString("> ", requestHeaders),
            headersToString("< ", responseHeaders),
            responseBody
        );
    }

    private static String headersToString(String prefix, Map<String, List<String>> headers) {
        return headers
            .entrySet()
            .stream()
            .flatMap(header -> header
                .getValue()
                .stream()
                .map(value -> {
                    if (header.getKey() == null) {
                        return prefix + value;
                    } else {
                        return prefix + (header.getKey() + ": ") + value;
                    }
                })
            ).collect(Collectors.joining("\n"));
    }
}
