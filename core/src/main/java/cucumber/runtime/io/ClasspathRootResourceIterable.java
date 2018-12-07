package cucumber.runtime.io;

import java.io.File;
import java.util.Iterator;

class ClasspathRootResourceIterable implements Iterable<Resource> {
    private final File root;
    private final File file;
    private final String suffix;

    ClasspathRootResourceIterable(File root, File file, String suffix) {
        this.root = root;
        this.file = file;
        this.suffix = suffix;
    }

    @Override
    public Iterator<Resource> iterator() {
        return FileResourceIterator.createClasspathFileResourceIterator(root, file, suffix);
    }
}
