package io.cucumber.core.plugin;

import io.cucumber.core.options.CurlOption;
import io.cucumber.core.options.CurlOption.HttpMethod;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.newOutputStream;
import static java.util.Objects.requireNonNull;

class UrlOutputStream extends OutputStream {

    private final UrlReporter urlReporter;

    private final CurlOption option;
    private final Path temp;
    private final OutputStream tempOutputStream;

    UrlOutputStream(CurlOption option, UrlReporter urlReporter) throws IOException {
        this.option = requireNonNull(option);
        this.urlReporter = urlReporter;
        this.temp = Files.createTempFile("cucumber", null);
        this.tempOutputStream = newOutputStream(temp);
    }

    @Override
    public void write(int b) throws IOException {
        tempOutputStream.write(b);
    }

    @Override
    public void write(byte[] buffer) throws IOException {
        tempOutputStream.write(buffer);
    }

    @Override
    public void write(byte[] buffer, int offset, int count) throws IOException {
        tempOutputStream.write(buffer, offset, count);
    }

    @Override
    public void flush() throws IOException {
        tempOutputStream.flush();
    }

    @Override
    public void close() throws IOException {
        tempOutputStream.close();
        sendRequest(option.getUri().toURL(), option.getMethod(), true)
                .ifPresent(redirectResponse -> {
                    if (urlReporter != null) {
                        urlReporter.report(redirectResponse);
                    }
                });
    }

    private Optional<String> sendRequest(URL url, HttpMethod method, boolean setHeaders) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        if (setHeaders) {
            for (Entry<String, String> header : option.getHeaders()) {
                urlConnection.setRequestProperty(header.getKey(), header.getValue());
            }
        }
        Map<String, List<String>> requestHeaders = urlConnection.getRequestProperties();
        urlConnection.setInstanceFollowRedirects(true);
        urlConnection.setRequestMethod(method.name());
        String redirectMessage = null;
        if (method == CurlOption.HttpMethod.GET) {
            redirectMessage = getResponseBody(urlConnection, requestHeaders);
            String location = urlConnection.getHeaderField("Location");
            if (urlConnection.getResponseCode() == 202 && location != null) {
                sendRequest(new URL(location), CurlOption.HttpMethod.PUT, false);
            }
        } else {
            urlConnection.setDoOutput(true);
            try (OutputStream outputStream = urlConnection.getOutputStream()) {
                Files.copy(temp, outputStream);
                getResponseBody(urlConnection, requestHeaders);
            }
        }
        return Optional.ofNullable(redirectMessage);
    }

    /**
     * return the request body
     *
     * @param  urlConnection  the http connection
     * @param  requestHeaders the headers sent
     * @return                the response body
     * @throws IOException    if an exception occurs
     */
    private static String getResponseBody(
            HttpURLConnection urlConnection, Map<String, List<String>> requestHeaders
    )
            throws IOException {
        Map<String, List<String>> responseHeaders = urlConnection.getHeaderFields();
        int responseCode = urlConnection.getResponseCode();
        boolean unsuccessful = responseCode >= 400;

        InputStream inputStream = urlConnection.getErrorStream() != null ? urlConnection.getErrorStream()
                : urlConnection.getInputStream();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, UTF_8))) {
            String responseBody = br.lines().collect(Collectors.joining(System.lineSeparator()));
            if (unsuccessful) {
                String method = urlConnection.getRequestMethod();
                URL url = urlConnection.getURL();
                throw createCurlLikeException(method, url, requestHeaders, responseHeaders, responseBody);
            } else {
                return responseBody;
            }
        }
    }

    static IOException createCurlLikeException(
            String method,
            URL url,
            Map<String, List<String>> requestHeaders,
            Map<String, List<String>> responseHeaders,
            String responseBody
    ) {
        return new IOException(String.format(
            "%s:\n> %s %s%s%s%s",
            "HTTP request failed",
            method,
            url,
            headersToString("> ", requestHeaders),
            headersToString("< ", responseHeaders),
            responseBody));
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
                            } else if (header.getValue() == null) {
                                return prefix + header.getKey();
                            } else {
                                return prefix + header.getKey() + ": " + value;
                            }
                        }))
                .collect(Collectors.joining("\n", "", "\n"));
    }

}
