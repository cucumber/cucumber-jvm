package cucumber.runtime.java;

import cucumber.runtime.CucumberException;
import cucumber.runtime.Glue;
import cucumber.runtime.HookDefinition;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.StepDefinitionMatch;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.java.stepdefs.Stepdefs;
import gherkin.I18n;
import gherkin.formatter.model.Step;
import org.junit.Test;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class JavaBackendTest {
    @Test
    public void finds_step_definitions_by_classpath_url() {
        ObjectFactory factory = new DefaultJavaObjectFactory();
        JavaBackend backend = new JavaBackend(factory);
        GlueStub glue = new GlueStub();
        backend.loadGlue(glue, asList("classpath:cucumber/runtime/java/stepdefs"));
        backend.buildWorld();
        assertEquals(Stepdefs.class, factory.getInstance(Stepdefs.class).getClass());
    }

    @Test
    public void finds_step_definitions_by_package_name() {
        ObjectFactory factory = new DefaultJavaObjectFactory();
        JavaBackend backend = new JavaBackend(factory);
        GlueStub glue = new GlueStub();
        backend.loadGlue(glue, asList("cucumber.runtime.java.stepdefs"));
        backend.buildWorld();
        assertEquals(Stepdefs.class, factory.getInstance(Stepdefs.class).getClass());
    }

    @Test(expected = CucumberException.class)
    public void detects_subclassed_glue_and_throws_exception() {
        ObjectFactory factory = new DefaultJavaObjectFactory();
        JavaBackend backend = new JavaBackend(factory);
        GlueStub glue = new GlueStub();
        backend.loadGlue(glue, asList("cucumber.runtime.java.stepdefs", "cucumber.runtime.java.incorrectlysubclassedstepdefs"));
    }

    private class GlueStub implements Glue {
        public final List<StepDefinition> stepDefinitions = new ArrayList<StepDefinition>();

        @Override
        public void addStepDefinition(StepDefinition stepDefinition) {
            stepDefinitions.add(stepDefinition);
        }

        @Override
        public void addBeforeAllHook(HookDefinition hookDefinition) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addBeforeFeatureHook(HookDefinition hookDefinition) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addBeforeHook(HookDefinition hookDefinition) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addAfterHook(HookDefinition hookDefinition) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addAfterFeatureHook(HookDefinition hookDefinition) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addAfterAllHook(HookDefinition hookDefinition) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<HookDefinition> getBeforeAllHooks() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<HookDefinition> getBeforeFeatureHooks() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<HookDefinition> getBeforeHooks() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<HookDefinition> getAfterHooks() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<HookDefinition> getAfterFeatureHooks() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<HookDefinition> getAfterAllHooks() {
            throw new UnsupportedOperationException();
        }

        @Override
        public StepDefinitionMatch stepDefinitionMatch(String featurePath, Step step, I18n i18n) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void writeStepdefsJson(ResourceLoader resourceLoader, List<String> featurePaths, URL dotCucumber) {
            throw new UnsupportedOperationException();
        }
    }
}
