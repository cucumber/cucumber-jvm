package cucumber.io;

import java.io.File;
import java.util.Iterator;

class FileResourceIterable implements Iterable<Resource> {
    private final File root;
    private final File file;
    private final String suffix;

    public FileResourceIterable(File root, File file, String suffix) {
        this.root = root;
        this.file = file;
        this.suffix = suffix;
    }

    @Override
    public Iterator<Resource> iterator() {
        return new FileResourceIterator(root, file, suffix);
    }
}
