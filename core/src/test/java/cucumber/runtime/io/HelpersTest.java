package cucumber.runtime.io;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import static cucumber.runtime.io.Helpers.filePath;
import static cucumber.runtime.io.Helpers.jarFilePath;
import static org.junit.Assert.assertEquals;

public class HelpersTest {
    private static final URLStreamHandler NULL_URL_STREAM_HANDLER = new URLStreamHandler() {
        @Override
        protected URLConnection openConnection(URL u) throws IOException {
            throw new UnsupportedOperationException();
        }
    };

    @Test
    public void computes_file_path_for_file_url() throws UnsupportedEncodingException, MalformedURLException {
        URL url = new URL("file:/Users/First%20Last/.m2/repository/info/cukes/cucumber-java/1.2.2/cucumber-java-1.2.2.jar");
        File fileFromFilePath = new File(filePath(url));
        File expectedFile = new File("/Users/First Last/.m2/repository/info/cukes/cucumber-java/1.2.2/cucumber-java-1.2.2.jar");
        assertEquals(expectedFile, fileFromFilePath);
    }

    @Test
    public void computes_file_path_for_jar_protocols() throws Exception {
        assertEquals("foo bar+zap/cucumber-core.jar", jarFilePath(new URL("jar:file:foo%20bar+zap/cucumber-core.jar!/cucumber/runtime/io")));
        assertEquals("foo bar+zap/cucumber-core.jar", jarFilePath(new URL(null, "zip:file:foo%20bar+zap/cucumber-core.jar!/cucumber/runtime/io", NULL_URL_STREAM_HANDLER)));
        assertEquals("foo bar+zap/cucumber-core.jar", jarFilePath(new URL(null, "wsjar:file:foo%20bar+zap/cucumber-core.jar!/cucumber/runtime/io", NULL_URL_STREAM_HANDLER)));
        assertEquals("foo bar+zap/cucumber-core.jar", jarFilePath(new URL("jar:file:foo%20bar+zap/cucumber-core.jar!/")));
        assertEquals("foo bar+zap/cucumber-core.jar", jarFilePath(new URL(null, "zip:file:foo%20bar+zap/cucumber-core.jar!/", NULL_URL_STREAM_HANDLER)));
        assertEquals("foo bar+zap/cucumber-core.jar", jarFilePath(new URL(null, "wsjar:file:foo%20bar+zap/cucumber-core.jar!/", NULL_URL_STREAM_HANDLER)));
    }
}
