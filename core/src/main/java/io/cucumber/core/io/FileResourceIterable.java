package io.cucumber.core.io;

import java.io.File;
import java.util.Iterator;

final class FileResourceIterable implements Iterable<Resource> {
    private final File root;
    private final File file;
    private final String suffix;

    FileResourceIterable(File root, File file, String suffix) {
        this.root = root;
        this.file = file;
        this.suffix = suffix;
    }

    @Override
    public Iterator<Resource> iterator() {
        return FileResourceIterator.createFileResourceIterator(root, file, suffix);
    }
}
