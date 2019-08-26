package cucumber.runtime.io;

import java.net.URL;
import java.util.Iterator;

/**
 * Resource iterator factory implementation which delegates to zip then file.
 */
public class ZipThenFileResourceIteratorFactory implements ResourceIteratorFactory {
    private final ResourceIteratorFactory zipResourceIteratorFactory = new ZipResourceIteratorFactory();
    private final ResourceIteratorFactory fileResourceIteratorFactory = new FileResourceIteratorFactory();

    @Override
    public boolean isFactoryFor(URL url) {
        return zipResourceIteratorFactory.isFactoryFor(url) || fileResourceIteratorFactory.isFactoryFor(url);
    }

    @Override
    public Iterator<Resource> createIterator(URL url, String path, String suffix) {
        if (zipResourceIteratorFactory.isFactoryFor(url)) {
            return zipResourceIteratorFactory.createIterator(url, path, suffix);
        } else {
            return fileResourceIteratorFactory.createIterator(url, path, suffix);
        }
    }
}
