package cucumber.runtime.io;

import gherkin.deps.com.google.gson.Gson;
import cucumber.util.FixJava;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

/**
 * A stream that can write to both file and http URLs. If it's a file URL, writes with a {@link java.io.FileOutputStream},
 * if it's a http or https URL, writes with a HTTP PUT (by default) or with the specified method.
 */
public class URLOutputStream extends OutputStream {
    private final URL url;
    private final String method;
    private final int expectedResponseCode;
    private final OutputStream out;
    private final HttpURLConnection urlConnection;

    public URLOutputStream(URL url) throws IOException {
        this(url, "PUT", Collections.<String, String>emptyMap(), 200);
    }

    public URLOutputStream(URL url, String method, Map<String, String> headers, int expectedResponseCode) throws IOException {
        this.url = url;
        this.method = method;
        this.expectedResponseCode = expectedResponseCode;
        if (url.getProtocol().equals("file")) {
            File file = new File(url.getFile());
            ensureParentDirExists(file);
            out = new FileOutputStream(file);
            urlConnection = null;
        } else if (url.getProtocol().startsWith("http")) {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(method);
            urlConnection.setDoOutput(true);
            for (Map.Entry<String, String> header : headers.entrySet()) {
                urlConnection.setRequestProperty(header.getKey(), header.getValue());
            }
            out = urlConnection.getOutputStream();
        } else {
            throw new IllegalArgumentException("URL Scheme must be one of file,http,https. " + url.toExternalForm());
        }
    }

    private void ensureParentDirExists(File file) throws IOException {
        if (file.getParentFile() != null && !file.getParentFile().isDirectory()) {
            boolean ok = file.getParentFile().mkdirs() || file.getParentFile().isDirectory();
            if (!ok) {
                throw new IOException("Failed to create directory " + file.getParentFile().getAbsolutePath());
            }
        }
    }

    @Override
    public void write(byte[] buffer, int offset, int count) throws IOException {
        out.write(buffer, offset, count);
    }

    @Override
    public void write(byte[] buffer) throws IOException {
        out.write(buffer);
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void close() throws IOException {
        try {
            if (urlConnection != null) {
                int responseCode = urlConnection.getResponseCode();
                if (responseCode != expectedResponseCode) {
                    try {
                        urlConnection.getInputStream().close();
                        throw new IOException(String.format("Expected response code: %d. Got: %d", expectedResponseCode, responseCode));
                    } catch (IOException expected) {
                        InputStream errorStream = urlConnection.getErrorStream();
                        if (errorStream != null) {
                            String responseBody = FixJava.readReader(new InputStreamReader(errorStream, "UTF-8"));
                            String contentType = urlConnection.getHeaderField("Content-Type");
                            if (contentType == null) {
                                contentType = "text/plain";
                            }
                            throw new ResponseException(responseBody, expected, responseCode, contentType);
                        } else {
                            throw expected;
                        }
                    }
                }
            }
        } finally {
            out.close();
        }
    }

    public class ResponseException extends IOException {
        private final Gson gson = new Gson();
        private final int responseCode;
        private final String contentType;

        public ResponseException(String responseBody, IOException cause, int responseCode, String contentType) {
            super(responseBody, cause);
            this.responseCode = responseCode;
            this.contentType = contentType;
        }

        @Override
        public String getMessage() {
            if (contentType.equals("application/json")) {
                Map map = gson.fromJson(super.getMessage(), Map.class);
                if (map.containsKey("error")) {
                    return getMessage0(map.get("error").toString());
                } else {
                    return getMessage0(super.getMessage());
                }
            } else {
                return getMessage0(super.getMessage());
            }
        }

        private String getMessage0(String message) {
            return String.format("%s %s\nHTTP %d\n%s", method, url, responseCode, message);
        }
    }
}
