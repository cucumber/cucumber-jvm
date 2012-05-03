package cucumber.io;

public class MultiLoader implements ResourceLoader {
    public static final String CLASSPATH_SCHEME = "classpath://";

    private final ClasspathResourceLoader classpath;
    private final FileResourceLoader fs;

    public MultiLoader(ClassLoader classLoader) {
        classpath = new ClasspathResourceLoader(classLoader);
        fs = new FileResourceLoader();
    }

    @Override
    public Iterable<Resource> resources(String path, String suffix) {
        if (path.startsWith(CLASSPATH_SCHEME)) {
            return classpath.resources(path.substring(CLASSPATH_SCHEME.length()), suffix);
        } else {
            return fs.resources(path, suffix);
        }
    }
}
