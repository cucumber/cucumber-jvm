package io.cucumber.core.runner;

import io.cucumber.core.eventbus.IncrementingUuidGenerator;
import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.plugin.StubTestCase;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UndefinedStepDefinitionMatchTest {

    private final Feature feature = TestFeatureParser.parse("" +
            "Feature: Test feature\n" +
            "  Scenario: Test scenario\n" +
            "     Given I have 4 cukes in my belly\n");

    private final UndefinedPickleStepDefinitionMatch match = new UndefinedPickleStepDefinitionMatch(
        URI.create("file:path/to.feature"),
        feature.getPickles().get(0).getSteps().get(0));
    private final TestCaseState mockTestCaseState = new TestCaseState(new StubEventBus(),
        new IncrementingUuidGenerator().generateId(), new StubTestCase());
    @Test
    void throws_undefined_step_definitions_exception_when_run() {
        UndefinedStepDefinitionException expectedThrown = assertThrows(UndefinedStepDefinitionException.class,
            () -> match.runStep(mockTestCaseState));
        assertThat(expectedThrown.getMessage(), equalTo("No step definitions found"));
    }

    @Test
    void throws_undefined_step_definitions_exception_when_dry_run() {
        UndefinedStepDefinitionException expectedThrown = assertThrows(UndefinedStepDefinitionException.class,
            () -> match.dryRunStep(mockTestCaseState));
        assertThat(expectedThrown.getMessage(), equalTo("No step definitions found"));
    }

}
