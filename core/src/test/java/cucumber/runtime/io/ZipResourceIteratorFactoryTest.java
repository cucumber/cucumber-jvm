package cucumber.runtime.io;

import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

// https://github.com/cucumber/cucumber-jvm/issues/808
public class ZipResourceIteratorFactoryTest {

    private static final URLStreamHandler NULL_URL_STREAM_HANDLER = new URLStreamHandler() {
        @Override
        protected URLConnection openConnection(URL u) throws IOException {
            throw new UnsupportedOperationException();
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
}
