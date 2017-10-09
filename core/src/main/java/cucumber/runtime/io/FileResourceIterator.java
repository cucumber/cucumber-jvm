package cucumber.runtime.io;

import java.io.File;
import java.io.FileFilter;
import java.util.Iterator;

import static cucumber.runtime.io.Helpers.hasSuffix;
import static java.util.Arrays.asList;

public class FileResourceIterator implements Iterator<Resource> {
    private final FlatteningIterator<Resource> flatteningIterator = new FlatteningIterator<Resource>();

    public static FileResourceIterator createFileResourceIterator(File root, File file, final String suffix) {
        return new FileResourceIterator(root, file, suffix, false);
    }

    public static FileResourceIterator createClasspathFileResourceIterator(File root, File file, final String suffix) {
        return new FileResourceIterator(root, file, suffix, true);
    }

    private FileResourceIterator(File root, File file, final String suffix, boolean classpathFileResourceIterator) {
        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory() || hasSuffix(suffix, file.getPath());
            }
        };
        flatteningIterator.push(new FileIterator(root, file, filter, classpathFileResourceIterator));
    }

    @Override
    public boolean hasNext() {
        return flatteningIterator.hasNext();
    }

    @Override
    public Resource next() {
        return flatteningIterator.next();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * Iterator to iterate over all the files contained in a directory. It returns
     * a File object for non directories or a new FileIterator object for directories.
     */
    private static class FileIterator implements Iterator<Object> {
        private final Iterator<File> files;
        private final FileFilter filter;
        private final File root;
        private final boolean classpathFileIterator;

        FileIterator(File root, File file, FileFilter filter, boolean classpathFileIterator) {
            this.root = root;
            if (file.isDirectory()) {
                this.files = asList(file.listFiles(filter)).iterator();
            } else if (file.isFile()) {
                this.files = asList(file).iterator();
            } else {
                throw new IllegalArgumentException("Not a file or directory: " + file.getAbsolutePath());
            }
            this.filter = filter;
            this.classpathFileIterator = classpathFileIterator;
            }

        @Override
        public Object next() {
            File next = files.next();

            if (next.isDirectory()) {
                return new FileIterator(root, next, filter, classpathFileIterator);
            } else {
                return createFileResource(next);
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

        private FileResource createFileResource(File next) {
            if (classpathFileIterator) {
                return FileResource.createClasspathFileResource(root, next);
            } else {
                return FileResource.createFileResource(root, next);
            }
        }
    }
}
