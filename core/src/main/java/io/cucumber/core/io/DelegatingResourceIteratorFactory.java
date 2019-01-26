package io.cucumber.core.io;

import io.cucumber.core.exception.CucumberException;

import java.net.URI;
import java.util.Iterator;
import java.util.ServiceLoader;


/**
 * A {@link ResourceIteratorFactory} implementation which delegates to
 * factories found by the ServiceLoader class.
 */
final class DelegatingResourceIteratorFactory implements ResourceIteratorFactory {

    private final Iterable<ResourceIteratorFactory> delegates = ServiceLoader.load(ResourceIteratorFactory.class);

    private final ResourceIteratorFactory fallbackResourceIteratorFactory;

    /**
     * Initializes a new instance of the DelegatingResourceIteratorFactory
     * class with a fallback factory.
     *
     * @param fallbackResourceIteratorFactory The factory to use when an
     *                                        appropriate one couldn't be found otherwise.
     */
    DelegatingResourceIteratorFactory(ResourceIteratorFactory fallbackResourceIteratorFactory) {
        this.fallbackResourceIteratorFactory = fallbackResourceIteratorFactory;
    }

    @Override
    public boolean isFactoryFor(URI url) {
        for (ResourceIteratorFactory delegate : delegates) {
            if (delegate.isFactoryFor(url)) {
                return true;
            }
        }
        return fallbackResourceIteratorFactory.isFactoryFor(url);
    }

    @Override
    public Iterator<Resource> createIterator(URI url, String path, String suffix) {
        for (ResourceIteratorFactory delegate : delegates) {
            if (delegate.isFactoryFor(url)) {
                return delegate.createIterator(url, path, suffix);
            }
        }
        if (fallbackResourceIteratorFactory.isFactoryFor(url)) {
            return fallbackResourceIteratorFactory.createIterator(url, path, suffix);
        } else {
            throw new CucumberException("Fallback factory cannot handle URL: " + url);
        }
    }
}
