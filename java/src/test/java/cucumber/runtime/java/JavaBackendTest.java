package cucumber.runtime.java;

import cucumber.fallback.runtime.java.DefaultJavaObjectFactory;
import cucumber.runtime.HookDefinition;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.World;
import cucumber.runtime.java.test.Stepdefs;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Step;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class JavaBackendTest {
    @Test
    public void finds_step_definitions_by_scanning_for_annotations() {
        ObjectFactory factory = new DefaultJavaObjectFactory();
        JavaBackend backend = new JavaBackend(factory);
        WorldStub world = new WorldStub();
        backend.loadGlue(world, asList("cucumber/runtime/java/test"));
        backend.buildWorld();
        assertEquals(Stepdefs.class, factory.getInstance(Stepdefs.class).getClass());
    }

    private class WorldStub implements World {
        public final List<StepDefinition> stepDefinitions = new ArrayList<StepDefinition>();


        @Override
        public void buildBackendContextAndRunBeforeHooks(Reporter reporter, Set<String> tags) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void runAfterHooksAndDisposeBackendContext(Reporter reporter, Set<String> tags) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void runStep(String uri, Step step, Reporter reporter, Locale locale) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void runUnreportedStep(String file, Locale locale, String stepKeyword, String stepName, int line) throws Throwable {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addStepDefinition(StepDefinition stepDefinition) {
            stepDefinitions.add(stepDefinition);
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
        public List<HookDefinition> getBeforeHooks() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<HookDefinition> getAfterHooks() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<StepDefinition> getStepDefinitions() {
            throw new UnsupportedOperationException();
        }
    }
}
