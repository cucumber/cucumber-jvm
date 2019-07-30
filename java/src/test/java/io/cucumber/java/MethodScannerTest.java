package io.cucumber.java;

import org.junit.Before;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MethodScannerTest {

    private final List<Map.Entry<Method, Annotation>> scanResult = new ArrayList<>();
    private BiConsumer<Method, Annotation> backend = (method, annotation) ->
        scanResult.add(new SimpleEntry<>(method, annotation));

    @Before
    public void createBackend() {

    }

    @Test
    public void scan_finds_annotated_methods() throws NoSuchMethodException {
        Method method = BaseStepDefs.class.getMethod("m");
        MethodScanner.scan(BaseStepDefs.class, backend);
        assertThat(scanResult, contains(new SimpleEntry<>(method, method.getAnnotations()[0])));
    }

    @Test
    public void scan_ignores_object() throws NoSuchMethodException {
        MethodScanner.scan(Object.class, backend);
        assertThat(scanResult, empty());
    }

    @Test
    public void loadGlue_fails_when_class_is_not_method_declaring_class() {
        InvalidMethodException exception = assertThrows(InvalidMethodException.class, () -> MethodScanner.scan(Stepdefs2.class, backend));
        assertThat(exception.getMessage(), is(
            "You're not allowed to extend classes that define Step Definitions or hooks. " +
                "class io.cucumber.java.MethodScannerTest$Stepdefs2 extends class io.cucumber.java.MethodScannerTest$BaseStepDefs"
        ));
    }

    public static class Stepdefs2 extends BaseStepDefs {
        public interface Interface1 {
        }
    }

    public static class BaseStepDefs {
        @io.cucumber.java.Before
        public void m() {
        }
    }
}
