package cucumber.runtime.io;

import static cucumber.runtime.io.ZipResourceIteratorFactory.filePath;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import org.junit.Test;

// https://github.com/cucumber/cucumber-jvm/issues/808
public class ZipResourceIteratorFactoryTest {

    private static final URLStreamHandler NULL_URL_STREAM_HANDLER = new URLStreamHandler() {
        @Override
        protected URLConnection openConnection(URL u) throws IOException {
            return null;
        }
    };

    @Test
    public void is_factory_for_jar_protocols() throws IOException {
        ZipResourceIteratorFactory factory = new ZipResourceIteratorFactory();

        assertTrue(factory.isFactoryFor(new URL("jar:file:cucumber-core.jar!/cucumber/runtime/io")));
        assertTrue(factory.isFactoryFor(new URL(null, "zip:file:cucumber-core.jar!/cucumber/runtime/io", NULL_URL_STREAM_HANDLER)));
        assertTrue(factory.isFactoryFor(new URL(null, "wsjar:file:cucumber-core.jar!/cucumber/runtime/io", NULL_URL_STREAM_HANDLER)));
        assertFalse(factory.isFactoryFor(new URL("file:cucumber-core")));
        assertFalse(factory.isFactoryFor(new URL("http://http://cukes.info/cucumber-core.jar")));
    }

    @Test
    public void computes_file_path_for_jar_protocols() throws Exception {
        assertEquals("cucumber-core.jar", filePath(new URL("jar:file:cucumber-core.jar!/cucumber/runtime/io")));
        assertEquals("cucumber-core.jar", filePath(new URL(null, "zip:file:cucumber-core.jar!/cucumber/runtime/io", NULL_URL_STREAM_HANDLER)));
        assertEquals("cucumber-core.jar", filePath(new URL(null, "wsjar:file:cucumber-core.jar!/cucumber/runtime/io", NULL_URL_STREAM_HANDLER)));
        assertEquals("cucumber-core.jar", filePath(new URL("jar:file:cucumber-core.jar!/")));
        assertEquals("cucumber-core.jar", filePath(new URL(null, "zip:file:cucumber-core.jar!/", NULL_URL_STREAM_HANDLER)));
        assertEquals("cucumber-core.jar", filePath(new URL(null, "wsjar:file:cucumber-core.jar!/", NULL_URL_STREAM_HANDLER)));
        assertEquals("cucumber-core.jar", filePath(new URL("file:cucumber-core.jar")));
    }
}
