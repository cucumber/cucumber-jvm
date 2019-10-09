package io.cucumber.java8;

import io.cucumber.core.backend.DataTableTypeDefinition;
import io.cucumber.core.backend.DocStringTypeDefinition;
import io.cucumber.core.backend.CucumberInvocationTargetException;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.ParameterTypeDefinition;
import io.cucumber.core.backend.StepDefinition;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Java8LambdaStepDefinitionMarksCorrectStackElementTest {

    private final MyLambdaGlueRegistry myLambdaGlueRegistry = new MyLambdaGlueRegistry();

    @Test
    void exception_from_step_should_be_defined_at_step_definition_class() {
        LambdaGlueRegistry.INSTANCE.set(myLambdaGlueRegistry);
        new SomeLambdaStepDefs();
        final StepDefinition stepDefinition = myLambdaGlueRegistry.getStepDefinition();

        CucumberInvocationTargetException exception = assertThrows(CucumberInvocationTargetException.class, () -> stepDefinition.execute(new Object[0]));
        assertThat(exception.getInvocationTargetExceptionCause(), new CustomTypeSafeMatcher<Throwable>("exception with matching stack trace") {
            @Override
            protected boolean matchesSafely(Throwable item) {
                return Arrays.stream(item.getStackTrace())
                    .filter(stepDefinition::isDefinedAt)
                    .findFirst()
                    .filter(stackTraceElement -> SomeLambdaStepDefs.class.getName().equals(stackTraceElement.getClassName()))
                    .isPresent();
            }
        });
    }

    private static class MyLambdaGlueRegistry implements LambdaGlueRegistry {

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

        @Override
        public void addDocStringType(DocStringTypeDefinition docStringTypeDefinition) {

        }

        @Override
        public void addDataTableType(DataTableTypeDefinition dataTableTypeDefinition) {

        }

        @Override
        public void addParameterType(ParameterTypeDefinition parameterTypeDefinition) {

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
