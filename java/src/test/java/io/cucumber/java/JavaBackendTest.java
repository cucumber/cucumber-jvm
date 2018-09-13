package io.cucumber.java;

import cucumber.api.java.ObjectFactory;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.io.MultiLoader;
import io.cucumber.core.io.ResourceLoader;
import io.cucumber.core.io.ResourceLoaderClassFinder;
import io.cucumber.java.stepdefs.Stepdefs;
import io.cucumber.core.stepexpression.TypeRegistry;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.Locale;

import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        backend.loadGlue(glue, asList("classpath:cucumber/runtime/java/stepdefs"));
        backend.buildWorld();
        assertEquals(Stepdefs.class, factory.getInstance(Stepdefs.class).getClass());
    }

    @Test
    public void finds_step_definitions_by_package_name() {
        GlueStub glue = new GlueStub();
        backend.loadGlue(glue, asList("io.cucumber.java.stepdefs"));
        backend.buildWorld();
        assertEquals(Stepdefs.class, factory.getInstance(Stepdefs.class).getClass());
    }

    @Test
    public void detects_subclassed_glue_and_throws_exception() {
        GlueStub glue = new GlueStub();
        final Executable testMethod = () -> backend.loadGlue(glue, asList("io.cucumber.java.stepdefs", "io.cucumber.java.incorrectlysubclassedstepdefs"));
        final CucumberException expectedThrown = assertThrows(CucumberException.class, testMethod);
        assertThat(expectedThrown.getMessage(), is(equalTo("You're not allowed to extend classes that define Step Definitions or hooks. class io.cucumber.java.incorrectlysubclassedstepdefs.SubclassesStepdefs extends class io.cucumber.java.stepdefs.Stepdefs")));
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

    }

}
