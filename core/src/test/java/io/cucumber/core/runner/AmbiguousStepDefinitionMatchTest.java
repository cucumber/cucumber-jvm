package io.cucumber.core.runner;

import io.cucumber.core.api.Scenario;
import io.cucumber.core.feature.CucumberFeature;
import io.cucumber.core.feature.CucumberStep;
import io.cucumber.core.feature.TestFeatureParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class AmbiguousStepDefinitionMatchTest {

    private final CucumberFeature feature = TestFeatureParser.parse("file:test.feature", "" +
        "Feature: Test feature\n" +
        "  Scenario: Test scenario\n" +
        "     Given I have 4 cukes in my belly\n"
    );
    private final CucumberStep step = feature.getPickles().get(0).getSteps().get(0);
    private final AmbiguousStepDefinitionsException e = new AmbiguousStepDefinitionsException(step, emptyList());
    private final AmbiguousPickleStepDefinitionsMatch match = new AmbiguousPickleStepDefinitionsMatch("file:test.feature", step, e);

    @Test
    void throws_ambiguous_step_definitions_exception_when_run() {
        Executable testMethod = () -> match.runStep(mock(Scenario.class));
        AmbiguousStepDefinitionsException actualThrown = assertThrows(AmbiguousStepDefinitionsException.class, testMethod);
        assertThat(actualThrown.getMessage(), is(equalTo(
            "\"I have 4 cukes in my belly\" matches more than one step definition:\n"
        )));
    }

    @Test
    void throws_ambiguous_step_definitions_exception_when_dry_run() {
        Executable testMethod = () -> match.dryRunStep(mock(Scenario.class));
        AmbiguousStepDefinitionsException actualThrown = assertThrows(AmbiguousStepDefinitionsException.class, testMethod);
        assertThat(actualThrown.getMessage(), is(equalTo(
            "\"I have 4 cukes in my belly\" matches more than one step definition:\n"
        )));
    }

}
