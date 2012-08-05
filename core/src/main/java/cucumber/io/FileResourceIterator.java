package cucumber.io;

import java.io.File;
import java.io.FileFilter;
import java.util.Iterator;

import static cucumber.io.ClasspathIterable.hasSuffix;
import static java.util.Arrays.asList;

class FileResourceIterator implements Iterator<Resource> {
    private final FlatteningIterator flatteningIterator = new FlatteningIterator();

    public FileResourceIterator(File root, File file, final String suffix) {
        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory() || hasSuffix(suffix, file.getPath());
            }
        };
        flatteningIterator.push(new FileIterator(root, file, filter));
    }

    @Override
    public boolean hasNext() {
        return flatteningIterator.hasNext();
    }

    @Override
    public Resource next() {
        return (Resource) flatteningIterator.next();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * Iterator to iterate over all the files contained in a directory. It returns
     * a File object for non directories or a new FileIterator obejct for directories.
     */
    private static class FileIterator implements Iterator<Object> {
        private final Iterator<File> files;
        private final FileFilter filter;
        private final File root;

        FileIterator(File root, File file, FileFilter filter) {
            this.root = root;
            if (file.isDirectory()) {
                this.files = asList(file.listFiles(filter)).iterator();
            } else if (file.isFile()) {
                this.files = asList(file).iterator();
            } else {
                throw new IllegalArgumentException("Not a file or directory: " + file.getAbsolutePath());
            }
            this.filter = filter;
        }

        @Override
        public Object next() {
            File next = files.next();

            if (next.isDirectory()) {
                return new FileIterator(root, next, filter);
            } else {
                return new FileResource(root, next);
            }
        }

        @Override
        public boolean hasNext() {
            return files.hasNext();
        }

        @Override
        public void remove() {
            files.remove();
        }
    }
}