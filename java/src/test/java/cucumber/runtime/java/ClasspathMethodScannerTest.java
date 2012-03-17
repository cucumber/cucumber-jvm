package cucumber.runtime.java;

import cucumber.annotation.Before;
import cucumber.io.ClasspathResourceLoader;
import cucumber.runtime.Glue;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;

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
        classpathMethodScanner.scan(backend, BaseStepDefs.class.getMethod("m"));

        verify(factory, times(1)).addClass(BaseStepDefs.class);
        verifyNoMoreInteractions(factory);
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
