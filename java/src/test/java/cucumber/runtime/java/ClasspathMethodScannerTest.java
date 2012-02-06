package cucumber.runtime.java;

import static java.util.Arrays.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;

import cucumber.annotation.Before;
import cucumber.io.ClasspathResourceLoader;
import cucumber.runtime.Glue;
import cucumber.runtime.java.test2.Stepdefs2;

/**
 * {description}
 * 
 * @version $Revision$
 */
@SuppressWarnings("unchecked")
public class ClasspathMethodScannerTest {

    @Test
    public void loadGlue_should_not_try_to_instantiate_super_classes() {

        ClasspathMethodScanner classpathMethodScanner = new ClasspathMethodScanner(new ClasspathResourceLoader(Thread.currentThread()
                .getContextClassLoader()));

        ObjectFactory factory = Mockito.mock(ObjectFactory.class);
        Glue world = Mockito.mock(Glue.class);
        JavaBackend backend = new JavaBackend(factory);
        Whitebox.setInternalState(backend, "glue", world);

        // this delegates to classpathMethodScanner.scan which we test
        classpathMethodScanner.scan(backend, asList("cucumber/runtime/java/test2"));

        Set<Class<?>> stepdefClasses = (Set<Class<?>>) Whitebox.getInternalState(backend, "stepDefinitionClasses");
        Assert.assertEquals(new HashSet<Class<?>>(Arrays.asList(Stepdefs2.class)), stepdefClasses);
    }

    public static class BaseStepDefs {

        @Before
        public void m() {
        }
    }
}
