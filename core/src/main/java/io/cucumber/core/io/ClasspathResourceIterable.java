package io.cucumber.core.io;

import io.cucumber.core.exception.CucumberException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;

import static io.cucumber.core.io.Classpath.resourceName;

final class ClasspathResourceIterable implements Iterable<Resource> {

    private final ResourceIteratorFactory resourceIteratorFactory =
        new DelegatingResourceIteratorFactory(new ZipThenFileResourceIteratorFactory());

    private final ClassLoader classLoader;
    private final String path;
    private final String suffix;

    ClasspathResourceIterable(ClassLoader classLoader, URI path, String suffix) {
        this.classLoader = classLoader;
        this.path = resourceName(path);
        this.suffix = suffix;
    }

    @Override
    public Iterator<Resource> iterator() {
        try {
            FlatteningIterator<Resource> iterator = new FlatteningIterator<>();
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
