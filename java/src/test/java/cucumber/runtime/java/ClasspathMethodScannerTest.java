package cucumber.runtime.java;

import cucumber.api.java.Before;
import cucumber.runtime.CucumberException;
import cucumber.runtime.Glue;
import cucumber.runtime.io.ClasspathResourceLoader;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class ClasspathMethodScannerTest {

    @Test
    public void loadGlue_registers_the_methods_declaring_class_in_the_object_factory() throws NoSuchMethodException {
        ClasspathResourceLoader resourceLoader = new ClasspathResourceLoader(Thread.currentThread().getContextClassLoader());
        ClasspathMethodScanner classpathMethodScanner = new ClasspathMethodScanner(resourceLoader);

        ObjectFactory factory = Mockito.mock(ObjectFactory.class);
        Glue world = Mockito.mock(Glue.class);
        JavaBackend backend = new JavaBackend(factory);
        Whitebox.setInternalState(backend, "glue", world);

        // this delegates to classpathMethodScanner.scan which we test
        classpathMethodScanner.scan(backend, BaseStepDefs.class.getMethod("m"), BaseStepDefs.class);

        verify(factory, times(1)).addClass(BaseStepDefs.class);
        verifyNoMoreInteractions(factory);
    }

    @Test
    public void loadGlue_fails_when_class_is_not_method_declaring_class() throws NoSuchMethodException {
        JavaBackend backend = new JavaBackend((ObjectFactory) null);
        try {
            backend.loadGlue(null, BaseStepDefs.class.getMethod("m"), Stepdefs2.class);
            fail();
        } catch (CucumberException e) {
            assertEquals("You're not allowed to extend classes that define Step Definitions or hooks. class cucumber.runtime.java.ClasspathMethodScannerTest$Stepdefs2 extends class cucumber.runtime.java.ClasspathMethodScannerTest$BaseStepDefs", e.getMessage());
        }
    }

    @Test
    public void loadGlue_fails_when_class_is_not_subclass_of_declaring_class() throws NoSuchMethodException {
        JavaBackend backend = new JavaBackend((ObjectFactory) null);
        try {
            backend.loadGlue(null, BaseStepDefs.class.getMethod("m"), String.class);
            fail();
        } catch (CucumberException e) {
            assertEquals("class cucumber.runtime.java.ClasspathMethodScannerTest$BaseStepDefs isn't assignable from class java.lang.String", e.getMessage());
        }
    }

    public static class Stepdefs2 extends BaseStepDefs {
        public interface Interface1 {
        }
    }

    public static class BaseStepDefs {
        @Before
        public void m() {
        }
    }
}
