package cucumber.runtime.io;

import java.io.File;
import java.net.URI;

public class ClasspathRootResourceLoader implements ResourceLoader {

    private final File root;

    public ClasspathRootResourceLoader(URI root) {
        this.root = new File(Helpers.filePath(root));
    }

    @Override
    public Iterable<Resource> resources(String path, String suffix) {
        return new ClasspathRootResourceIterable(root, root, suffix);
    }
}
