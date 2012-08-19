package cucumber.io;

import static cucumber.io.ClasspathIterable.getPath;

import java.io.File;
import java.net.URL;
import java.util.Iterator;

/**
 * Factory which creates {@link FileResourceIterator}s.
 * 
 * <p>{@link FileResourceIterator}s should be created for any cases where a
 * URL's protocol isn't otherwise handled. Thus, {@link #isFactoryFor(URL)}
 * will always return <code>true</code>. Because of this behavior, the
 * <code>FileResourceIteratorFactory</code> should never be registered as a
 * service implementation for {@link ResourceIteratorFactory} as it could
 * easily hide other service implementations.</p>
 */
public class FileResourceIteratorFactory implements ResourceIteratorFactory {
    
    /**
     * Initializes a new instance of the FileResourceIteratorFactory class.
     */
    public FileResourceIteratorFactory() {
        // intentionally empty
    }
    
    @Override
    public boolean isFactoryFor(URL url) {
        return true;
    }
    
    @Override
    public Iterator<Resource> createIterator(URL url, String path, String suffix) {
        File file = new File(getPath(url));
        File rootDir = new File(file.getAbsolutePath().substring(0, file.getAbsolutePath().length() - path.length()));
        return new FileResourceIterator(rootDir, file, suffix);
    }
}
