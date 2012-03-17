package cucumber.runtime.java;

import cucumber.annotation.Before;
import cucumber.io.ClasspathResourceLoader;
import cucumber.runtime.Glue;
import cucumber.runtime.java.test2.Stepdefs2;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class ClasspathMethodScannerTest {

    @Test
    public void loadGlue_should_not_try_to_instantiate_super_classes() {

        ClasspathResourceLoader resourceLoader = new ClasspathResourceLoader(Thread.currentThread().getContextClassLoader());
        ClasspathMethodScanner classpathMethodScanner = new ClasspathMethodScanner(resourceLoader);

        ObjectFactory factory = Mockito.mock(ObjectFactory.class);
        Glue world = Mockito.mock(Glue.class);
        JavaBackend backend = new JavaBackend(factory);
        Whitebox.setInternalState(backend, "glue", world);

        // this delegates to classpathMethodScanner.scan which we test
        classpathMethodScanner.scan(backend, asList("cucumber/runtime/java/test2"));

        verify(factory, times(1)).addClass(Stepdefs2.class);
        verifyNoMoreInteractions(factory);
    }

    public static class BaseStepDefs {

        @Before
        public void m() {
        }
    }
}
