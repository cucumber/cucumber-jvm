package cucumber.runtime.io;

import java.net.URL;
import java.util.Iterator;

public class TestResourceIteratorFactory implements ResourceIteratorFactory {
    /**
     * Initializes a new instance of the TestResourceIteratorFactory class.
     */
    public TestResourceIteratorFactory() {
        // intentionally empty
    }

    @Override
    public boolean isFactoryFor(URL url) {
        return "file".equals(url.getProtocol()) && url.getPath().endsWith("test");
    }

    @Override
    public Iterator<Resource> createIterator(URL url, String path, String suffix) {
        return new TestResourceIterator();
    }

}
