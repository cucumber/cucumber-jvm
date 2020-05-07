package io.cucumber.core.resource;

import io.cucumber.core.resource.test.ExampleClass;
import io.cucumber.core.resource.test.ExampleInterface;
import io.cucumber.core.resource.test.OtherClass;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

class ClasspathScannerTest {

    private final ClasspathScanner scanner = new ClasspathScanner(
        ClasspathScannerTest.class::getClassLoader);

    @Test
    void scanForSubClassesInPackage() {
        List<Class<? extends ExampleInterface>> classes = scanner.scanForSubClassesInPackage("io.cucumber",
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

}
