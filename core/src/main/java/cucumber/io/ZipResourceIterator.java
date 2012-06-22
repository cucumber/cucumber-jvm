package cucumber.io;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

class ZipResourceIterator implements Iterator<Resource> {
    private final String path;
    private final String suffix;
    private final ZipFile jarFile;
    private final Enumeration<? extends ZipEntry> entries;
    private Resource next;

    public ZipResourceIterator(String zipPath, String path, String suffix) throws IOException {
        this.path = path;
        this.suffix = suffix;
        jarFile = new ZipFile(zipPath);
        entries = jarFile.entries();

        moveToNext();
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public Resource next() {
        try {
            if (next == null) {
                throw new NoSuchElementException();
            }
            return next;
        } finally {
            moveToNext();
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    private void moveToNext() {
        next = null;
        while (entries.hasMoreElements()) {
            ZipEntry jarEntry = entries.nextElement();
            String entryName = jarEntry.getName();
            if (entryName.startsWith(path) && ClasspathIterable.hasSuffix(suffix, entryName)) {
                next = new ZipResource(jarFile, jarEntry);
                break;
            }
        }
    }
}
