package cucumber.runtime.io;

import java.net.URL;

import java.util.Iterator;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * @author mdelapenya
 */
public class BundleResourceIteratorFactory implements ResourceIteratorFactory {

    @Override
    public boolean isFactoryFor(URL url) {
        String protocol = url.getProtocol();

        return protocol.matches("bundle(resource)?");
    }

    @Override
    public Iterator<Resource> createIterator(
        URL url, String path, String suffix) {

        Bundle bundle = FrameworkUtil.getBundle(this.getClass());

        return new BundleResourceIterator(bundle, suffix);
    }

}
