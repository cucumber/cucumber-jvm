package cucumber.runtime.io;

import cucumber.runtime.Utils;
import gherkin.util.FixJava;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A Writer that will write to an URL. If it's a file URL, writes with a {@link UTF8OutputStreamWriter},
 * if it's a http or https URL, writes with a HTTP PUT.
 */
public class URLOutputStream extends OutputStream {
    private OutputStream out;
    private HttpURLConnection urlConnection;

    public URLOutputStream(URL url) throws IOException {
        if (url.getProtocol().equals("file")) {
            File file = new File(url.getFile());
            Utils.ensureParentDirExists(file);
            out = new FileOutputStream(file);
        } else if (url.getProtocol().startsWith("http")) {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("PUT");
            urlConnection.setDoOutput(true);
            out = urlConnection.getOutputStream();
        } else {
            throw new IllegalArgumentException("URL Scheme must be one of file,http,https. " + url.toExternalForm());
        }
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
        if (urlConnection != null) {
            int responseCode = urlConnection.getResponseCode();
            if (responseCode >= 400) {
                try {
                    urlConnection.getInputStream().close();
                } catch (IOException e) {
                    InputStream errorStream = urlConnection.getErrorStream();
                    if (errorStream != null) {
                        String body = FixJava.readReader(new InputStreamReader(errorStream, "UTF-8"));
                        throw new IOException(body, e);
                    } else {
                        throw e;
                    }
                }
            }
        }
        out.close();
    }
}
