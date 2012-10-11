package cucumber.runtime.io;

import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import static org.junit.Assert.assertTrue;

public class DelegatingResourceIteratorFactoryTest {

    @Test
    public void should_load_test_resource_iterator() throws MalformedURLException {
        ResourceIteratorFactory factory = new DelegatingResourceIteratorFactory();
        URL url = new URL("file:///this/is/only/a/test");

        assertTrue(factory.isFactoryFor(url));

        Iterator<Resource> iterator = factory.createIterator(url, "test", "test");

        assertTrue(iterator instanceof TestResourceIterator);
    }
}
