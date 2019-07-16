package cucumber.runtime.java;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cucumber.api.java.ObjectFactory;
import cucumber.runtime.CucumberException;
import cucumber.runtime.Env;
import cucumber.runtime.Glue;
import cucumber.runtime.HookDefinition;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.runtime.java.stepdefs.Stepdefs;
import io.cucumber.stepexpression.TypeRegistry;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.Locale;

import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;

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
        GlueStub glue = new GlueStub();
        backend.loadGlue(glue, asList(URI.create("classpath:cucumber/runtime/java/stepdefs")));
        backend.buildWorld();
        assertEquals(Stepdefs.class, factory.getInstance(Stepdefs.class).getClass());
    }

    @Test(expected = CucumberException.class)
    public void detects_subclassed_glue_and_throws_exception() {
        GlueStub glue = new GlueStub();
        backend.loadGlue(glue, asList(
            URI.create("classpath:cucumber/runtime/java/stepdefs"),
            URI.create("classpath:cucumber/runtime/java/incorrectlysubclassedstepdefs"))
        );
    }

    @Test
    public void testObjectFactoryClassNameWhenNotSpecified() {
        assertNull(JavaBackend.getObjectFactoryClassName(mock(Env.class)));
    }

    @Test
    public void testGetObjectFactoryClassName() {
        Env env = when(mock(Env.class).get(JavaBackend.OBJECT_FACTORY_KEY)).thenReturn("AN_OBJECT_FACTORY").getMock();
        assertEquals("AN_OBJECT_FACTORY", JavaBackend.getObjectFactoryClassName(env));
    }

    @Test
    public void testDeprecatedObjectFactoryClassNameWhenNotSpecified() {
        assertNull(JavaBackend.getDeprecatedObjectFactoryClassName(mock(Env.class)));
    }

    @Test
    public void testGetDeprecatedObjectFactoryClassName() {
        Env env = when(mock(Env.class).get(ObjectFactory.class.getName())).thenReturn("DEPRECATED_OBJECT_FACTORY").getMock();
        assertEquals("DEPRECATED_OBJECT_FACTORY", JavaBackend.getDeprecatedObjectFactoryClassName(env));
    }

    private class GlueStub implements Glue {

        @Override
        public void addStepDefinition(StepDefinition stepDefinition) {
            //no-op
        }

        @Override
        public void addBeforeStepHook(HookDefinition beforeStepHook) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addBeforeHook(HookDefinition hookDefinition) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addAfterStepHook(HookDefinition hookDefinition) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addAfterHook(HookDefinition hookDefinition) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void removeScenarioScopedGlue() {
        }
    }
}
