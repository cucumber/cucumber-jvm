package cucumber.runtime.io;

import cucumber.runtime.CucumberException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;

import static cucumber.runtime.io.MultiLoader.CLASSPATH_SCHEME;

class ClasspathResourceIterable implements Iterable<Resource> {
    private final ResourceIteratorFactory resourceIteratorFactory =
            new DelegatingResourceIteratorFactory(new ZipThenFileResourceIteratorFactory());

    private final ClassLoader classLoader;
    private final String path;
    private final String suffix;

    ClasspathResourceIterable(ClassLoader classLoader, URI path, String suffix) {
        this.classLoader = classLoader;


        if(!CLASSPATH_SCHEME.equals(path.getScheme())){
            throw new IllegalArgumentException("path must have classpath scheme " + path);
        }

        this.path = path.getSchemeSpecificPart();
        this.suffix = suffix;
    }

    @Override
    public Iterator<Resource> iterator() {
        try {
            FlatteningIterator<Resource> iterator = new FlatteningIterator<Resource>();
            Enumeration<URL> resources = classLoader.getResources(path);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                Iterator<Resource> resourceIterator = resourceIteratorFactory.createIterator(url.toURI(), path, suffix);
                iterator.push(resourceIterator);
            }
            return iterator;
        } catch (IOException | URISyntaxException e) {
            throw new CucumberException(e);
        }
    }

}
