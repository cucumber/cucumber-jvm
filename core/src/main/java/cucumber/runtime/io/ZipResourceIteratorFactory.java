package cucumber.runtime.io;

import static cucumber.runtime.io.ClasspathIterable.filePath;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

import cucumber.runtime.CucumberException;

/**
 * Factory which creates {@link ZipResourceIterator}s for URL's with the jar
 * protocols ("jar", "zip", "wsjar" and "vfszip")
 */
public class ZipResourceIteratorFactory implements ResourceIteratorFactory {

    /**
     * Initializes a new instance of the ZipResourceIteratorFactory class.
     */
    public ZipResourceIteratorFactory() {
        // intentionally empty
    }

    @Override
    public boolean isFactoryFor(URL url) {
        return ResourceUtils.isJarURL(url);
    }

    @Override
    public Iterator<Resource> createIterator(URL url, String path, String suffix) {
        try {
            String jarPath = filePath(url);
            return new ZipResourceIterator(jarPath, path, suffix);
        } catch (IOException e) {
            throw new CucumberException(e);
        }
    }
}
