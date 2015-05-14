package cucumber.runtime.io;

import cucumber.runtime.CucumberException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.Iterator;

public class ClasspathResourceIterable implements Iterable<Resource> {
    private final ResourceIteratorFactory resourceIteratorFactory =
            new DelegatingResourceIteratorFactory(new ZipThenFileResourceIteratorFactory());

    private final ClassLoader classLoader;
    private final String path;
    private final String suffix;

    public ClasspathResourceIterable(ClassLoader classLoader, String path, String suffix) {
        this.classLoader = classLoader;
        this.path = path;
        this.suffix = suffix;
    }

    @Override
    public Iterator<Resource> iterator() {
        try {
            FlatteningIterator<Resource> iterator = new FlatteningIterator<Resource>();
            Enumeration<URL> resources = classLoader.getResources(path);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                Iterator<Resource> resourceIterator = resourceIteratorFactory.createIterator(url, path, suffix);
                iterator.push(resourceIterator);
            }
            return iterator;
        } catch (IOException e) {
            throw new CucumberException(e);
        }
    }

}
