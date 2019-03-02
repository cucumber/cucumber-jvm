package cucumber.runtime.io;

import java.io.File;
import java.io.IOException;
import java.net.URI;
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

    ZipResourceIterator(URI zipPath, String path, String suffix) throws IOException {
        this.path = path;
        this.suffix = suffix;
        jarFile = new ZipFile(new File(zipPath));
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
            if (entryName.startsWith(path) && Helpers.hasSuffix(suffix, entryName)) {
                next = new ZipResource(jarFile, jarEntry);
                break;
            }
        }
    }
}
