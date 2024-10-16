package io.cucumber.core.runner;

import io.cucumber.core.backend.CucumberBackendException;
import io.cucumber.core.backend.CucumberInvocationTargetException;
import io.cucumber.core.backend.ParameterInfo;
import io.cucumber.core.backend.StepDefinition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertAll;

class DuplicateStepDefinitionExceptionTest {

    @Test
    void can_report_duplicate_step_definitions() {
        DuplicateStepDefinitionException expectedThrown = new DuplicateStepDefinitionException(
            new StubStepDefinition("StepDefinitionA_Location"),
            new StubStepDefinition("StepDefinitionB_Location"));
        assertAll(
            () -> assertThat(expectedThrown.getMessage(),
                is(equalTo("Duplicate step definitions in StepDefinitionA_Location and StepDefinitionB_Location"))),
            () -> assertThat(expectedThrown.getCause(), is(nullValue())));
    }

    public static class StubStepDefinition implements StepDefinition {
        private final String location;

        public StubStepDefinition(String location) {
            this.location = location;
        }

        @Override
        public boolean isDefinedAt(StackTraceElement stackTraceElement) {
            return false;
        }

        @Override
        public String getLocation() {
            return location;
        }

        @Override
        public void execute(Object[] args) throws CucumberBackendException, CucumberInvocationTargetException {

        }

        @Override
        public List<ParameterInfo> parameterInfos() {
            return null;
        }

        @Override
        public String getPattern() {
            return null;
        }
    }
}
