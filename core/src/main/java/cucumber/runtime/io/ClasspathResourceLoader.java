package cucumber.runtime.io;

import java.net.URI;

public class ClasspathResourceLoader implements ResourceLoader {
    private final ClassLoader classLoader;

    public ClasspathResourceLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public Iterable<Resource> resources(URI path, String suffix) {
        return new ClasspathResourceIterable(classLoader, path, suffix);
    }
}
