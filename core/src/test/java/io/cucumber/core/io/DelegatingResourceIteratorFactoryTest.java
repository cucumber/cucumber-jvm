package io.cucumber.core.io;

import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Iterator;

import static org.junit.Assert.assertTrue;

public class DelegatingResourceIteratorFactoryTest {

    @Test
    public void should_load_test_resource_iterator() throws MalformedURLException {
        ResourceIteratorFactory factory =
            new DelegatingResourceIteratorFactory(new ZipThenFileResourceIteratorFactory());
        URI url = URI.create(TestResourceIteratorFactory.TEST_URL);

        assertTrue(factory.isFactoryFor(url));

        Iterator<Resource> iterator = factory.createIterator(url, "test", "test");

        assertTrue(iterator instanceof TestResourceIterator);
    }

}
