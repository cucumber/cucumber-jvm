package cucumber.runtime.io;

public class ClasspathResourceLoader implements ResourceLoader {
    private final ClassLoader classLoader;

    public ClasspathResourceLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public Iterable<Resource> resources(String path, String suffix) {
        return new ClasspathResourceIterable(classLoader, path, suffix);
    }
}
