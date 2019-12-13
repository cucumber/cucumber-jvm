package io.cucumber.core.runner;

import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.feature.TestFeatureParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class UndefinedStepDefinitionMatchTest {

    private final Feature feature = TestFeatureParser.parse("" +
        "Feature: Test feature\n" +
        "  Scenario: Test scenario\n" +
        "     Given I have 4 cukes in my belly\n"
    );

    private final UndefinedPickleStepDefinitionMatch match = new UndefinedPickleStepDefinitionMatch(
        URI.create("file:path/to.feature"),
        feature.getPickles().get(0).getSteps().get(0)
    );

    @Test
    void throws_ambiguous_step_definitions_exception_when_run() {
        Executable testMethod = () -> match.runStep(mock(TestCaseState.class));
        UndefinedStepDefinitionException expectedThrown = assertThrows(UndefinedStepDefinitionException.class, testMethod);
        assertThat(expectedThrown.getMessage(), is(equalTo("No step definitions found")));
    }

    @Test
    void throws_ambiguous_step_definitions_exception_when_dry_run() {
        Executable testMethod = () -> match.dryRunStep(mock(TestCaseState.class));
        UndefinedStepDefinitionException expectedThrown = assertThrows(UndefinedStepDefinitionException.class, testMethod);
        assertThat(expectedThrown.getMessage(), is(equalTo("No step definitions found")));
    }

}
