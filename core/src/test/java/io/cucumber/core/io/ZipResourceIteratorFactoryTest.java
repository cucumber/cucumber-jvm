package io.cucumber.core.io;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

// https://github.com/cucumber/cucumber-jvm/issues/808
public class ZipResourceIteratorFactoryTest {

    @Test
    public void is_factory_for_jar_protocols() throws IOException {
        ZipResourceIteratorFactory factory = new ZipResourceIteratorFactory();

        assertTrue(factory.isFactoryFor(URI.create("jar:file:cucumber-core.jar!/cucumber/runtime/io")));
        assertTrue(factory.isFactoryFor(URI.create("zip:file:cucumber-core.jar!/cucumber/runtime/io")));
        assertTrue(factory.isFactoryFor(URI.create("wsjar:file:cucumber-core.jar!/cucumber/runtime/io")));
        assertFalse(factory.isFactoryFor(URI.create("file:cucumber-core")));
        assertFalse(factory.isFactoryFor(URI.create("http://http://cukes.info/cucumber-core.jar")));
    }

}
