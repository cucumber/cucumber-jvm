package io.cucumber.java;

import io.cucumber.core.stepexpression.TypeRegistry;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.backend.Glue;
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

import java.util.Collections;
import java.util.Locale;

import static java.lang.Thread.currentThread;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class MethodScannerTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private ObjectFactory factory;

    private ResourceLoaderClassFinder classFinder;
    private JavaBackend backend;

    @Before
    public void createBackend(){
        ClassLoader classLoader = currentThread().getContextClassLoader();
        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        this.classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        TypeRegistry typeRegistry = new TypeRegistry(Locale.ENGLISH);
        this.backend = new JavaBackend(factory, classFinder, typeRegistry);
    }

    @Test
    public void loadGlue_registers_the_methods_declaring_class_in_the_object_factory() throws NoSuchMethodException {
        MethodScanner methodScanner = new MethodScanner(classFinder);
        Glue world = Mockito.mock(Glue.class);
        backend.loadGlue(world,Collections.<String>emptyList());

        // this delegates to methodScanner.scan which we test
        methodScanner.scan(backend, BaseStepDefs.class.getMethod("m"), BaseStepDefs.class);

        verify(factory, times(1)).addClass(BaseStepDefs.class);
        verifyNoMoreInteractions(factory);
    }

    @Test
    public void loadGlue_fails_when_class_is_not_method_declaring_class() throws NoSuchMethodException {
        try {
            backend.loadGlue(null, BaseStepDefs.class.getMethod("m"), Stepdefs2.class);
            fail();
        } catch (CucumberException e) {
            assertEquals("You're not allowed to extend classes that define Step Definitions or hooks. class io.cucumber.java.MethodScannerTest$Stepdefs2 extends class io.cucumber.java.MethodScannerTest$BaseStepDefs", e.getMessage());
        }
    }

    @Test
    public void loadGlue_fails_when_class_is_not_subclass_of_declaring_class() throws NoSuchMethodException {
        try {
            backend.loadGlue(null, BaseStepDefs.class.getMethod("m"), String.class);
            fail();
        } catch (CucumberException e) {
            assertEquals("class io.cucumber.java.MethodScannerTest$BaseStepDefs isn't assignable from class java.lang.String", e.getMessage());
        }
    }

    public static class Stepdefs2 extends BaseStepDefs {
        public interface Interface1 {
        }
    }

    public static class BaseStepDefs {
        @io.cucumber.java.api.Before
        public void m() {
        }
    }
}
