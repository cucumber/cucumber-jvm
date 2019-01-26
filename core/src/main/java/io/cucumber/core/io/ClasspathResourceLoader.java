package io.cucumber.core.io;

import java.net.URI;

final class ClasspathResourceLoader implements ResourceLoader {
    private final ClassLoader classLoader;

    ClasspathResourceLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public Iterable<Resource> resources(URI path, String suffix) {
        return new ClasspathResourceIterable(classLoader, path, suffix);
    }
}
