package cucumber.runtime.io;

import java.net.URL;
import java.util.Iterator;

/**
 * Resource iterator factory implementation which acts as a fallback when no
 * other factories are found.
 */
public class ZipThenFileResourceIteratorFallback implements ResourceIteratorFactory {
    /**
     * The file resource iterator factory.
     */
    private final FileResourceIteratorFactory fileResourceIteratorFactory;

    /**
     * The ZIP resource iterator factory.
     */
    private final ZipResourceIteratorFactory zipResourceIteratorFactory;


    /**
     * Initializes a new instance of the ZipThenFileResourceIteratorFallback
     * class.
     */
    public ZipThenFileResourceIteratorFallback() {
        fileResourceIteratorFactory = new FileResourceIteratorFactory();
        zipResourceIteratorFactory = new ZipResourceIteratorFactory();
    }

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
