package io.cucumber.core.runner;

import gherkin.pickles.PickleStep;
import io.cucumber.core.api.Scenario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

public class UndefinedStepDefinitionMatchTest {

    public final UndefinedPickleStepDefinitionMatch match = new UndefinedPickleStepDefinitionMatch(mock(PickleStep.class));

    @Test
    public void throws_ambiguous_step_definitions_exception_when_run() {
        Executable testMethod = () -> match.runStep(mock(Scenario.class));
        UndefinedStepDefinitionException expectedThrown = assertThrows(UndefinedStepDefinitionException.class, testMethod);
        assertThat(expectedThrown.getMessage(), is(equalTo("No step definitions found")));
    }

    @Test
    public void throws_ambiguous_step_definitions_exception_when_dry_run() {
        Executable testMethod = () -> match.dryRunStep(mock(Scenario.class));
        UndefinedStepDefinitionException expectedThrown = assertThrows(UndefinedStepDefinitionException.class, testMethod);
        assertThat(expectedThrown.getMessage(), is(equalTo("No step definitions found")));
    }

}
