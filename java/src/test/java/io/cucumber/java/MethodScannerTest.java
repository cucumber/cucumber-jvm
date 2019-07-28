package io.cucumber.java;

import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.io.MultiLoader;
import io.cucumber.core.io.ResourceLoader;
import io.cucumber.core.io.ResourceLoaderClassFinder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.net.URI;
import java.util.Collections;

import static java.lang.Thread.currentThread;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class MethodScannerTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private ObjectFactory factory;

    private ResourceLoaderClassFinder classFinder;
    private JavaBackend backend;

    @Before
    public void createBackend() {
        ClassLoader classLoader = currentThread().getContextClassLoader();
        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        this.classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        this.backend = new JavaBackend(factory, factory, classFinder);
    }

    @Test
    public void loadGlue_registers_the_methods_declaring_class_in_the_object_factory() throws NoSuchMethodException {
        MethodScanner methodScanner = new MethodScanner(classFinder);
        Glue world = Mockito.mock(Glue.class);
        backend.loadGlue(world, Collections.<URI>emptyList());

        // this delegates to methodScanner.scan which we test
        methodScanner.scan(backend, BaseStepDefs.class.getMethod("m"), BaseStepDefs.class);

        verify(factory, times(1)).addClass(BaseStepDefs.class);
        verifyNoMoreInteractions(factory);
    }

    @Test
    public void loadGlue_fails_when_class_is_not_method_declaring_class() throws NoSuchMethodException {
        MethodScanner methodScanner = new MethodScanner(classFinder);
        InvalidMethodException exception = assertThrows(InvalidMethodException.class, () -> methodScanner.scan(backend, BaseStepDefs.class.getMethod("m"), Stepdefs2.class));
        assertThat(exception.getMessage(), is(
            "You're not allowed to extend classes that define Step Definitions or hooks. " +
                "class io.cucumber.java.MethodScannerTest$Stepdefs2 extends class io.cucumber.java.MethodScannerTest$BaseStepDefs"
        ));
    }

    @Test
    public void loadGlue_fails_when_class_is_not_subclass_of_declaring_class() throws NoSuchMethodException {
        MethodScanner methodScanner = new MethodScanner(classFinder);
        InvalidMethodException exception = assertThrows(InvalidMethodException.class, () -> methodScanner.scan(backend, BaseStepDefs.class.getMethod("m"), String.class));
        assertThat(exception.getMessage(), is(
            "class io.cucumber.java.MethodScannerTest$BaseStepDefs isn't assignable from class java.lang.String"
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
