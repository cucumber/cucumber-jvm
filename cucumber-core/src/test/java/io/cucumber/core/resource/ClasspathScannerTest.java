package io.cucumber.core.resource;

import io.cucumber.core.logging.LogRecordListener;
import io.cucumber.core.logging.WithLogRecordListener;
import io.cucumber.core.resource.test.ExampleClass;
import io.cucumber.core.resource.test.ExampleInterface;
import io.cucumber.core.resource.test.OtherClass;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import static java.util.Collections.enumeration;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

@WithLogRecordListener
class ClasspathScannerTest {

    private final ClasspathScanner scanner = new ClasspathScanner(
        ClasspathScannerTest.class::getClassLoader);

    @Test
    void scanForSubClassesInPackage() {
        List<Class<? extends ExampleInterface>> classes = scanner.scanForSubClassesInPackage(
            "io.cucumber.core.resource.test",
            ExampleInterface.class);

        assertThat(classes, contains(ExampleClass.class));
    }

    @Test
    void scanForSubClassesInNonExistingPackage() {
        List<Class<? extends ExampleInterface>> classes = scanner
                .scanForSubClassesInPackage("io.cucumber.core.resource.does.not.exist", ExampleInterface.class);
        assertThat(classes, empty());
    }

    @Test
    void scanForClassesInPackage() {
        List<Class<?>> classes = scanner.scanForClassesInPackage("io.cucumber.core.resource.test");

        assertThat(classes, containsInAnyOrder(
            ExampleClass.class,
            ExampleInterface.class,
            OtherClass.class));

    }

    @Test
    void scanForClassesInNonExistingPackage() {
        List<Class<?>> classes = scanner.scanForClassesInPackage("io.cucumber.core.resource.does.not.exist");
        assertThat(classes, empty());
    }

    @Test
    void scanForResourcesInUnsupportedFileSystem(LogRecordListener logRecordListener) throws IOException {
        URLStreamHandler handler = new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL u) {
                return null;
            }
        };
        URL resourceUrl = new URL(null, "bundle-resource:com/cucumber/bundle", handler);
        ClassLoader classLoader = new MockClassLoader(
            Map.of("com/cucumber/bundle", enumeration(singletonList(resourceUrl))));
        ClasspathScanner scanner = new ClasspathScanner(() -> classLoader);
        assertThat(scanner.scanForClassesInPackage("com.cucumber.bundle"), empty());
        assertThat(logRecordListener.getLogRecords().get(0).getMessage(),
            containsString("Failed to find resources for 'bundle-resource:com/cucumber/bundle'"));
    }

    public static class MockClassLoader extends ClassLoader {
        private final Map<String, Enumeration<URL>> resources;

        public MockClassLoader(Map<String, Enumeration<URL>> resources) {
            this.resources = resources;
        }

        @Override
        public Enumeration<URL> getResources(String name) {
            return resources.get(name);
        }
    }

}
