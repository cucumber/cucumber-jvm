package cucumber.runtime.java8;

import cucumber.api.StepDefinitionReporter;
import cucumber.runtime.DuplicateStepDefinitionException;
import cucumber.runtime.Glue;
import cucumber.runtime.HookDefinition;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.StepDefinitionMatch;
import cucumber.runtime.java.LambdaGlueRegistry;
import gherkin.pickles.PickleStep;
import org.hamcrest.CustomTypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;

public class Java8LambdaStepDefinitionMarksCorrectStackElementTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private final MyLambdaGlueRegistry myLambdaGlueRegistry = new MyLambdaGlueRegistry();
    private final Glue glue = new StubGlue();

    @Test
    public void exception_from_step_should_be_defined_at_step_definition_class() throws Throwable {
        LambdaGlueRegistry.INSTANCE.set(myLambdaGlueRegistry);
        LambdaGlueRegistry.GLUE.set(glue);
        new SomeLambdaStepDefs();
        final StepDefinition stepDefinition = myLambdaGlueRegistry.getStepDefinition();

        expectedException.expect(new CustomTypeSafeMatcher<Throwable>("exception with matching stack trace") {
            @Override
            protected boolean matchesSafely(Throwable item) {
                for (StackTraceElement stackTraceElement : item.getStackTrace()) {
                    if(stepDefinition.isDefinedAt(stackTraceElement)){
                        return SomeLambdaStepDefs.class.getName().equals(stackTraceElement.getClassName());
                    }

                }
                return false;
            }
        });

        stepDefinition.execute("en", new Object[0]);
    }


    private class MyLambdaGlueRegistry implements LambdaGlueRegistry {

        private StepDefinition stepDefinition;

        @Override
        public void addStepDefinition(Glue glue, StepDefinition stepDefinition) {
            this.stepDefinition = stepDefinition;
        }

        @Override
        public void addBeforeHookDefinition(Glue glue, HookDefinition beforeHook) {

        }

        @Override
        public void addAfterHookDefinition(Glue glue, HookDefinition afterHook) {

        }

        public StepDefinition getStepDefinition() {
            return stepDefinition;
        }
    }
    
    private class StubGlue implements Glue {

        @Override
        public Glue clone() {
            return null;
        }

        @Override
        public void addStepDefinition(final StepDefinition stepDefinition) throws DuplicateStepDefinitionException {

        }

        @Override
        public void addBeforeHook(final HookDefinition hookDefinition) {

        }

        @Override
        public void addAfterHook(final HookDefinition hookDefinition) {

        }

        @Override
        public void reportStepDefinitions(final StepDefinitionReporter stepDefinitionReporter) {

        }

        @Override
        public List<HookDefinition> getBeforeHooks() {
            return null;
        }

        @Override
        public List<HookDefinition> getAfterHooks() {
            return null;
        }

        @Override
        public StepDefinitionMatch stepDefinitionMatch(final String featurePath, final PickleStep step) {
            return null;
        }

        @Override
        public void removeScenarioScopedGlue() {

        }
    }
}
