package cucumber.io;

import java.net.URL;
import java.util.Iterator;
import java.util.ServiceLoader;

import cucumber.runtime.CucumberException;


/**
 * A {@link ResourceIteratorFactory} implementation which delegates to
 * factories found by the ServiceLoader class.
 */
public class DelegatingResourceIteratorFactory implements ResourceIteratorFactory {

    /**
     * The delegates.
     */
    private final Iterable<ResourceIteratorFactory> delegates;
    
    /**
     * The fallback resource iterator factory.
     */
    private final ResourceIteratorFactory fallback;
    
    /**
     * Initializes a new instance of the DelegatingResourceIteratorFactory
     * class.
     */
    public DelegatingResourceIteratorFactory() {
        this(new ZipThenFileResourceIteratorFallback());
    }
    
    /**
     * Initializes a new instance of the DelegatingResourceIteratorFactory
     * class with a fallback factory.
     * 
     * @param fallback The fallback resource iterator factory to use when an
     * appropriate one couldn't be found otherwise.
     */
    public DelegatingResourceIteratorFactory(ResourceIteratorFactory fallback) {
        delegates = ServiceLoader.load(ResourceIteratorFactory.class);
        this.fallback = fallback;
    }
    
    @Override
    public boolean isFactoryFor(URL url) {
        for (ResourceIteratorFactory delegate : delegates) {
            if (delegate.isFactoryFor(url)) {
                return true;
            }
        }
        return fallback.isFactoryFor(url);
    }
    
    @Override
    public Iterator<Resource> createIterator(URL url, String path, String suffix) {
        for (ResourceIteratorFactory delegate : delegates) {
            if (delegate.isFactoryFor(url)) {
                return delegate.createIterator(url, path, suffix);
            }
        }
        if (fallback.isFactoryFor(url)) {
            return fallback.createIterator(url, path, suffix);
        } else {
            throw new CucumberException("Fallback factory cannot handle URL: " + url);
        }
    }
}
