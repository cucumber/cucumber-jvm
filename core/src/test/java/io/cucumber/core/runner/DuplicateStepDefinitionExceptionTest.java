package io.cucumber.core.runner;

import io.cucumber.core.backend.StepDefinition;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DuplicateStepDefinitionExceptionTest {

    @Test
    void can_report_duplicate_step_definitions() {
        final StepDefinition mockStepDefinitionA = mock(StepDefinition.class);
        when(mockStepDefinitionA.getLocation()).thenReturn("StepDefinitionA_Location");
        final StepDefinition mockStepDefinitionB = mock(StepDefinition.class);
        when(mockStepDefinitionB.getLocation()).thenReturn("StepDefinitionB_Location");

        DuplicateStepDefinitionException expectedThrown = new DuplicateStepDefinitionException(mockStepDefinitionA,
            mockStepDefinitionB);
        assertAll(
            () -> assertThat(expectedThrown.getMessage(),
                is(equalTo("Duplicate step definitions in StepDefinitionA_Location and StepDefinitionB_Location"))),
            () -> assertThat(expectedThrown.getCause(), is(nullValue())));
    }

}
