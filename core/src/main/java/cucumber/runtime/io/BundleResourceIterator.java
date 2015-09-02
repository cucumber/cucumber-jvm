package cucumber.runtime.io;

import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.osgi.framework.Bundle;

/**
 * @author mdelapenya
 */
public class BundleResourceIterator implements Iterator<Resource> {

    private final URL bundlePath;
    private final Bundle bundle;
    private final Enumeration<URL> entries;
    private Resource next;
    private String suffix;

    public BundleResourceIterator(Bundle bundle, String suffix) {
        this.bundle = bundle;
        this.bundlePath = bundle.getEntry("/");
        this.entries = this.bundle.findEntries("/", null, true);
        this.suffix = suffix;

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
        }
        finally {
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
            URL urlEntry = entries.nextElement();

            String entryPath = urlEntry.getPath();

            if (entryPath.endsWith(suffix)) {
                next = new BundleResource(bundlePath, urlEntry);
                break;
            }
        }
    }

}
