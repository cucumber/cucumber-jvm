package io.cucumber.core.runner;

import io.cucumber.core.api.Scenario;
import io.cucumber.core.feature.CucumberFeature;
import io.cucumber.core.feature.TestFeatureParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class FailedStepInstantiationMatchTest {
    private final CucumberFeature feature = TestFeatureParser.parse(
        "file:test.feature",
        "Feature: Test feature\n" +
            "  Scenario: Test scenario\n" +
            "     Given I have 4 cukes in my belly\n"
    );
    private final Exception exception = new Exception();
    private FailedPickleStepInstantiationMatch match = new FailedPickleStepInstantiationMatch(
        "file:test.feature",
        feature.getPickles().get(0).getSteps().get(0),
        exception
    );

    @Test
    void throws_the_exception_passed_to_the_match_when_run() {
        Executable testMethod = () -> match.runStep(mock(Scenario.class));
        Exception expectedThrown = assertThrows(Exception.class, testMethod);
        assertThat(expectedThrown, is(exception));
    }

    @Test
    void throws_the_exception_passed_to_the_match_when_dry_run() {
        Executable testMethod = () -> match.dryRunStep(mock(Scenario.class));
        Exception expectedThrown = assertThrows(Exception.class, testMethod);
        assertThat(expectedThrown, is(exception));
    }

}
