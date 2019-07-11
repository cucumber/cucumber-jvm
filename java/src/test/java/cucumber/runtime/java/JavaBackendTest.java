package cucumber.runtime.java;

import cucumber.runtime.CucumberException;
import cucumber.runtime.Glue;
import cucumber.runtime.HookDefinition;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.runtime.java.stepdefs.Stepdefs;
import io.cucumber.core.backend.ObjectFactory;
import io.cucumber.stepexpression.TypeRegistry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.net.URI;
import java.util.Locale;

import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class JavaBackendTest {

    private ObjectFactory factory;
    private JavaBackend backend;

    @Before
    public void createBackend() {
        ClassLoader classLoader = currentThread().getContextClassLoader();
        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        ResourceLoaderClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        this.factory = new DefaultJavaObjectFactory();
        TypeRegistry typeRegistry = new TypeRegistry(Locale.ENGLISH);
        this.backend = new JavaBackend(factory, classFinder, typeRegistry);
    }

    @Test
    public void finds_step_definitions_by_classpath_url() {
        backend.loadGlue(Mockito.mock(Glue.class), asList(URI.create("classpath:cucumber/runtime/java/stepdefs")));
        backend.buildWorld();
        assertEquals(Stepdefs.class, factory.getInstance(Stepdefs.class).getClass());
    }

    @Test(expected = CucumberException.class)
    public void detects_subclassed_glue_and_throws_exception() {
        backend.loadGlue(Mockito.mock(Glue.class), asList(
            URI.create("classpath:cucumber/runtime/java/stepdefs"),
            URI.create("classpath:cucumber/runtime/java/incorrectlysubclassedstepdefs"))
        );
    }
}
