package io.cucumber.core.io;

import java.io.File;
import java.net.URI;
import java.util.Iterator;

/**
 * Factory which creates {@link FileResourceIterator}s.
 * <p/>
 * <p>{@link FileResourceIterator}s should be created for any cases where a
 * URL's protocol isn't otherwise handled. Thus, {@link #isFactoryFor(URI)}
 * will always return <code>true</code>. Because of this behavior, the
 * <code>FileResourceIteratorFactory</code> should never be registered as a
 * service implementation for {@link ResourceIteratorFactory} as it could
 * easily hide other service implementations.</p>
 */
final class FileResourceIteratorFactory implements ResourceIteratorFactory {

    @Override
    public boolean isFactoryFor(URI url) {
        return true;
    }

    @Override
    public Iterator<Resource> createIterator(URI url, String path, String suffix) {


        File file = new File(url);
        File rootDir = new File(file.getAbsolutePath().substring(0, file.getAbsolutePath().length() - path.length()));
        return FileResourceIterator.createClasspathFileResourceIterator(rootDir, file, suffix);
    }
}
