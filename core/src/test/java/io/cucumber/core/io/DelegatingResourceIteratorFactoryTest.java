package io.cucumber.core.io;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Iterator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.isA;
import static org.junit.Assert.assertTrue;

public class DelegatingResourceIteratorFactoryTest {

    @Test
    public void should_load_test_resource_iterator() {
        ResourceIteratorFactory factory =
            new DelegatingResourceIteratorFactory(new ZipThenFileResourceIteratorFactory());
        URI url = URI.create(TestResourceIteratorFactory.TEST_URL);

        assertTrue(factory.isFactoryFor(url));

        Iterator<Resource> iterator = factory.createIterator(url, "test", "test");

        assertThat(iterator, isA(TestResourceIterator.class));
    }

}
