package io.cucumber.core.io;

import java.net.URI;
import java.util.Iterator;

/**
 * Resource iterator factory implementation which delegates to zip then file.
 */
final class ZipThenFileResourceIteratorFactory implements ResourceIteratorFactory {
    private final ResourceIteratorFactory zipResourceIteratorFactory = new ZipResourceIteratorFactory();
    private final ResourceIteratorFactory fileResourceIteratorFactory = new FileResourceIteratorFactory();

    @Override
    public boolean isFactoryFor(URI url) {
        return zipResourceIteratorFactory.isFactoryFor(url) || fileResourceIteratorFactory.isFactoryFor(url);
    }

    @Override
    public Iterator<Resource> createIterator(URI url, String path, String suffix) {
        if (zipResourceIteratorFactory.isFactoryFor(url)) {
            return zipResourceIteratorFactory.createIterator(url, path, suffix);
        } else {
            return fileResourceIteratorFactory.createIterator(url, path, suffix);
        }
    }
}
