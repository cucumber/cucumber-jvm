package io.cucumber.java8;

import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.StepDefinition;
import org.hamcrest.CustomTypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;

public class Java8LambdaStepDefinitionMarksCorrectStackElementTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private final MyLambdaGlueRegistry myLambdaGlueRegistry = new MyLambdaGlueRegistry();

    @Test
    public void exception_from_step_should_be_defined_at_step_definition_class() throws Throwable {
        LambdaGlueRegistry.INSTANCE.set(myLambdaGlueRegistry);
        new SomeLambdaStepDefs();
        final StepDefinition stepDefinition = myLambdaGlueRegistry.getStepDefinition();

        expectedException.expect(new CustomTypeSafeMatcher<Throwable>("exception with matching stack trace") {
            @Override
            protected boolean matchesSafely(Throwable item) {
                return Arrays.stream(item.getStackTrace())
                    .filter(stepDefinition::isDefinedAt)
                    .findFirst()
                    .filter(stackTraceElement -> SomeLambdaStepDefs.class.getName().equals(stackTraceElement.getClassName()))
                    .isPresent();
            }
        });

        stepDefinition.execute(new Object[0]);
    }


    private class MyLambdaGlueRegistry implements LambdaGlueRegistry {

        private StepDefinition stepDefinition;

        @Override
        public void addStepDefinition(StepDefinition stepDefinition) {
            this.stepDefinition = stepDefinition;
        }

        @Override
        public void addBeforeStepHookDefinition(HookDefinition beforeStepHook) {

        }

        @Override
        public void addBeforeHookDefinition(HookDefinition beforeHook) {

        }

        @Override
        public void addAfterStepHookDefinition(HookDefinition afterStepHook) {

        }

        @Override
        public void addAfterHookDefinition(HookDefinition afterHook) {

        }

        StepDefinition getStepDefinition() {
            return stepDefinition;
        }
    }

    public static final class SomeLambdaStepDefs implements En {

        public SomeLambdaStepDefs() {
            Given("I have a some step definition", () -> {
                throw new Exception();
            });
        }

    }
}
