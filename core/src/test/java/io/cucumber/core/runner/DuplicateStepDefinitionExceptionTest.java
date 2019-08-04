package io.cucumber.core.runner;

import io.cucumber.core.backend.StepDefinition;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.Executable;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DuplicateStepDefinitionExceptionTest {

    @TestFactory
    public Collection<DynamicTest> exceptions() {

        return Arrays.asList(

            DynamicTest.dynamicTest("StepDefinition null, StepDefinition null", () -> {
                Executable testMethod = () -> {
                    throw new DuplicateStepDefinitionException(null, null);
                };
                DuplicateStepDefinitionException expectedThrown = assertThrows(DuplicateStepDefinitionException.class, testMethod);
                assertAll(
                    () -> assertThat(expectedThrown.getMessage(), is(equalTo("Duplicate step definitions in \"null step definition\" and \"null step definition\""))),
                    () -> assertThat(expectedThrown.getCause(), is(nullValue()))
                );
            }),

            DynamicTest.dynamicTest("StepDefinition mock, StepDefinition mock", () -> {
                Executable testMethod = () -> {
                    final StepDefinition mockStepDefinitionA = mock(StepDefinition.class);
                    when(mockStepDefinitionA.getLocation(true))
                        .thenReturn("StepDefinitionA_Location");
                    final StepDefinition mockStepDefinitionB = mock(StepDefinition.class);
                    when(mockStepDefinitionB.getLocation(true))
                        .thenReturn("StepDefinitionB_Location");
                    throw new DuplicateStepDefinitionException(mockStepDefinitionA, mockStepDefinitionB);
                };
                DuplicateStepDefinitionException expectedThrown = assertThrows(DuplicateStepDefinitionException.class, testMethod);
                assertAll(
                    () -> assertThat(expectedThrown.getMessage(), is(equalTo("Duplicate step definitions in StepDefinitionA_Location and StepDefinitionB_Location"))),
                    () -> assertThat(expectedThrown.getCause(), is(nullValue()))
                );
            })

        );
    }

}
