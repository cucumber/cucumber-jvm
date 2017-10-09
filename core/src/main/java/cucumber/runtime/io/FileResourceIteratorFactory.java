package cucumber.runtime.io;

import java.io.File;
import java.net.URL;
import java.util.Iterator;

import static cucumber.runtime.io.Helpers.filePath;

/**
 * Factory which creates {@link FileResourceIterator}s.
 * <p/>
 * <p>{@link FileResourceIterator}s should be created for any cases where a
 * URL's protocol isn't otherwise handled. Thus, {@link #isFactoryFor(URL)}
 * will always return <code>true</code>. Because of this behavior, the
 * <code>FileResourceIteratorFactory</code> should never be registered as a
 * service implementation for {@link ResourceIteratorFactory} as it could
 * easily hide other service implementations.</p>
 */
public class FileResourceIteratorFactory implements ResourceIteratorFactory {

    @Override
    public boolean isFactoryFor(URL url) {
        return true;
    }

    @Override
    public Iterator<Resource> createIterator(URL url, String path, String suffix) {
        File file = new File(filePath(url));
        File rootDir = new File(file.getAbsolutePath().substring(0, file.getAbsolutePath().length() - path.length()));
        return FileResourceIterator.createClasspathFileResourceIterator(rootDir, file, suffix);
    }
}
