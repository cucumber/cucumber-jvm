package cucumber.runtime.io;

import java.net.URL;
import java.util.Iterator;

public class TestResourceIteratorFactory implements ResourceIteratorFactory {
    public static final String TEST_URL = "file:/this/is/only/a/test";

    @Override
    public boolean isFactoryFor(URL url) {
        return url.toExternalForm().equals(TEST_URL);
    }

    @Override
    public Iterator<Resource> createIterator(URL url, String path, String suffix) {
        return new TestResourceIterator();
    }

}
