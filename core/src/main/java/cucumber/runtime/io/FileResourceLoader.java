package cucumber.runtime.io;

import java.io.File;

class FileResourceLoader implements ResourceLoader {
    @Override
    public Iterable<Resource> resources(String path, String suffix) {
        File root = new File(path);
        return new FileResourceIterable(root, root, suffix);
    }
}
