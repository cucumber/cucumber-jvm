package cucumber.runtime.io;

import java.net.URI;
import java.net.URL;
import java.util.Iterator;

public class TestResourceIteratorFactory implements ResourceIteratorFactory {
    static final String TEST_URL = "file:/this/is/only/a/test";

    @Override
    public boolean isFactoryFor(URI url) {
        return TEST_URL.equals(url.toString());
    }

    @Override
    public Iterator<Resource> createIterator(URI url, String path, String suffix) {
        return new TestResourceIterator();
    }

}
