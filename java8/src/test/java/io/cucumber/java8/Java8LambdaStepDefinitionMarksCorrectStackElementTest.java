package io.cucumber.java8;

import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.StepDefinition;
import org.hamcrest.CustomTypeSafeMatcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Java8LambdaStepDefinitionMarksCorrectStackElementTest {

    private final MyLambdaGlueRegistry myLambdaGlueRegistry = new MyLambdaGlueRegistry();

    @Test
    public void exception_from_step_should_be_defined_at_step_definition_class() {
        LambdaGlueRegistry.INSTANCE.set(myLambdaGlueRegistry);
        new SomeLambdaStepDefs();
        final StepDefinition stepDefinition = myLambdaGlueRegistry.getStepDefinition();

        final Executable testMethod = () -> stepDefinition.execute(new Object[0]);
        final Exception actualThrown = assertThrows(Exception.class, testMethod);
        assertAll("Checking Exception including cause",
            () -> assertThat("Unexpected exception message", actualThrown.getMessage(), is(nullValue())),
            () -> assertThat("Unexpected exception StackTrace", actualThrown,
                new CustomTypeSafeMatcher<Throwable>("exception with matching stack trace") {
                    @Override
                    protected boolean matchesSafely(Throwable item) {
                        for (final StackTraceElement stackTraceElement : item.getStackTrace()) {
                            if (stepDefinition.isDefinedAt(stackTraceElement)) {
                                return SomeLambdaStepDefs.class.getName().equals(stackTraceElement.getClassName());
                            }

                        }
                        return false;
                    }
                })
        );
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

    static final class SomeLambdaStepDefs implements En {

        SomeLambdaStepDefs() {
            Given("I have a some step definition", () -> {
                throw new Exception();
            });
        }

    }

}
