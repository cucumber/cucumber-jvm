package io.cucumber.core.resource;

import io.cucumber.core.logging.LogRecordListener;
import io.cucumber.core.logging.LoggerFactory;
import io.cucumber.core.resource.test.ExampleClass;
import io.cucumber.core.resource.test.ExampleInterface;
import io.cucumber.core.resource.test.OtherClass;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.List;

import static java.util.Collections.enumeration;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClasspathScannerTest {

    private final ClasspathScanner scanner = new ClasspathScanner(
        ClasspathScannerTest.class::getClassLoader);

    private LogRecordListener logRecordListener;

    @BeforeEach
    void setup() {
        logRecordListener = new LogRecordListener();
        LoggerFactory.addListener(logRecordListener);
    }

    @AfterEach
    void tearDown() {
        LoggerFactory.removeListener(logRecordListener);
    }

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
    void scanForSubClassesWhenPackage() {
        List<Class<? extends ExampleInterface>> classes = scanner.scanForSubClasses(
            "io.cucumber.core.resource.test",
            ExampleInterface.class);

        assertThat(classes, contains(ExampleClass.class));
    }

    @Test
    void scanForSubClassesWhenClass() {
        List<Class<? extends ExampleInterface>> classes = scanner.scanForSubClasses(
            "io.cucumber.core.resource.test.ExampleClass",
            ExampleInterface.class);

        assertThat(classes, contains(ExampleClass.class));
    }

    @Test
    void scanForSubClassesWhenNonExistingPackage() {
        List<Class<? extends ExampleInterface>> classes = scanner
                .scanForSubClasses("io.cucumber.core.resource.does.not.exist", ExampleInterface.class);
        assertThat(classes, empty());
    }

    @Test
    void scanForSubClassesWhenNonExistingClass() {
        List<Class<? extends ExampleInterface>> classes = scanner
                .scanForSubClasses("io.cucumber.core.resource.test.NonExistentClass", ExampleInterface.class);
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
    void scanForResourcesInUnsupportedFileSystem() throws IOException {
        ClassLoader classLoader = mock(ClassLoader.class);
        ClasspathScanner scanner = new ClasspathScanner(() -> classLoader);
        URLStreamHandler handler = new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL u) {
                return null;
            }
        };
        URL resourceUrl = new URL(null, "bundle-resource:com/cucumber/bundle", handler);
        when(classLoader.getResources("com/cucumber/bundle")).thenReturn(enumeration(singletonList(resourceUrl)));
        assertThat(scanner.scanForClassesInPackage("com.cucumber.bundle"), empty());
        assertThat(logRecordListener.getLogRecords().get(0).getMessage(),
            containsString("Failed to find resources for 'bundle-resource:com/cucumber/bundle'"));
    }

    @Test
    void getClassesWhenPackage() {
        List<Class<?>> classes = scanner.getClasses("io.cucumber.core.resource.test");

        assertThat(classes, containsInAnyOrder(
            ExampleClass.class,
            ExampleInterface.class,
            OtherClass.class));

    }

    @Test
    void getClassesWhenNonExistingPackage() {
        List<Class<?>> classes = scanner.getClasses("io.cucumber.core.resource.does.not.exist");
        assertThat(classes, empty());
    }

    @Test
    void getClassesWhenClass() {
        List<Class<?>> classes = scanner.getClasses("io.cucumber.core.resource.test.ExampleClass");

        assertThat(classes, contains(ExampleClass.class));

    }

    @Test
    void getClassesWhenNonExistingClass() {
        List<Class<?>> classes = scanner.getClasses("io.cucumber.core.resource.test.NonExistentClass");
        assertThat(classes, empty());
    }
}
