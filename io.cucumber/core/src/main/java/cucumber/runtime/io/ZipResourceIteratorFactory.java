package cucumber.runtime.io;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

import cucumber.runtime.CucumberException;

/**
 * Factory which creates {@link ZipResourceIterator}s for URL's with "jar", "zip" and "wsjar"
 * protocols.
 */
public class ZipResourceIteratorFactory implements ResourceIteratorFactory {

    @Override
    public boolean isFactoryFor(URL url) {
        return url.getFile().contains("!/");
    }

    @Override
    public Iterator<Resource> createIterator(URL url, String path, String suffix) {
        try {
            String jarPath = Helpers.jarFilePath(url);
            return new ZipResourceIterator(jarPath, path, suffix);
        } catch (IOException e) {
            throw new CucumberException(e);
        }
    }
}
