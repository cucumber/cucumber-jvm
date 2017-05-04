package cucumber.runtime.io;

import org.junit.Rule;
import org.junit.Test;
import org.osgi.framework.Bundle;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author mdelapenya
 */
// https://github.com/cucumber/cucumber-jvm/issues/900
public class BundleResourceIteratorFactoryTest {

    @Rule
    public ModuleFrameworkTestRule moduleFrameworkTestRule =
        new ModuleFrameworkTestRule();

    private static final URLStreamHandler NULL_URL_STREAM_HANDLER = new URLStreamHandler() {
        @Override
        protected URLConnection openConnection(URL u) throws IOException {
            throw new UnsupportedOperationException();
        }
    };

    @Test
    public void is_factory_for_jar_protocols() throws IOException {
        BundleResourceIteratorFactory factory = new BundleResourceIteratorFactory();

        Bundle bundle = moduleFrameworkTestRule.getTestBundle();

        URL bundleEntry = bundle.getEntry("org/eclipse/equinox/http/servlet/HttpServiceServlet.class");

        assertTrue(factory.isFactoryFor(bundleEntry));
        assertFalse(factory.isFactoryFor(new URL(null, "zip:file:cucumber-core.jar!/cucumber/runtime/io", NULL_URL_STREAM_HANDLER)));
        assertFalse(factory.isFactoryFor(new URL(null, "wsjar:file:cucumber-core.jar!/cucumber/runtime/io", NULL_URL_STREAM_HANDLER)));
        assertFalse(factory.isFactoryFor(new URL("file:cucumber-core")));
        assertFalse(factory.isFactoryFor(new URL("http://http://cukes.info/cucumber-core.jar")));
    }
}
