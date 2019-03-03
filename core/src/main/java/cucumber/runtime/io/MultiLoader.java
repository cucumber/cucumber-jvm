package cucumber.runtime.io;

import java.net.URI;

import static io.cucumber.core.model.Classpath.CLASSPATH_SCHEME;

public class MultiLoader implements ResourceLoader {
    static final String FILE_SCHEME = "file";

    private final ClasspathResourceLoader classpath;
    private final FileResourceLoader fs;

    public MultiLoader(ClassLoader classLoader) {
        classpath = new ClasspathResourceLoader(classLoader);
        fs = new FileResourceLoader();
    }

    @Override
    public Iterable<Resource> resources(URI path, String suffix) {
        if (CLASSPATH_SCHEME.equals(path.getScheme())) {
            return classpath.resources(path, suffix);
        } else if (FILE_SCHEME.equals(path.getScheme())) {
            return fs.resources(path, suffix);
        } else {
            throw new IllegalArgumentException("Unsupported scheme: " + path);
        }
    }

}
