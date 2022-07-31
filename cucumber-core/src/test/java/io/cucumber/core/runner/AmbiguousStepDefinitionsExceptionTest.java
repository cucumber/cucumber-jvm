package io.cucumber.core.runner;

import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Step;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AmbiguousStepDefinitionsExceptionTest {

    @Test
    void can_report_ambiguous_step_definitions() {
        Feature feature = TestFeatureParser.parse("" +
                "Feature: Test feature\n" +
                "  Scenario: Test scenario\n" +
                "     Given I have 4 cukes in my belly\n");

        Step mockPickleStep = feature.getPickles().get(0).getSteps().get(0);

        PickleStepDefinitionMatch mockPickleStepDefinitionMatchOne = mock(PickleStepDefinitionMatch.class);
        when(mockPickleStepDefinitionMatchOne.getPattern()).thenReturn("PickleStepDefinitionMatchOne_Pattern");
        when(mockPickleStepDefinitionMatchOne.getLocation()).thenReturn("PickleStepDefinitionMatchOne_Location");

        PickleStepDefinitionMatch mockPickleStepDefinitionMatchTwo = mock(PickleStepDefinitionMatch.class);
        when(mockPickleStepDefinitionMatchTwo.getPattern()).thenReturn("PickleStepDefinitionMatchTwo_Pattern");
        when(mockPickleStepDefinitionMatchTwo.getLocation()).thenReturn("PickleStepDefinitionMatchTwo_Location");

        List<PickleStepDefinitionMatch> matches = asList(mockPickleStepDefinitionMatchOne,
            mockPickleStepDefinitionMatchTwo);

        AmbiguousStepDefinitionsException expectedThrown = new AmbiguousStepDefinitionsException(mockPickleStep,
            matches);
        assertAll(
            () -> assertThat(expectedThrown.getMessage(), is(equalTo(
                "" +
                        "\"I have 4 cukes in my belly\" matches more than one step definition:\n" +
                        "  \"PickleStepDefinitionMatchOne_Pattern\" in PickleStepDefinitionMatchOne_Location\n" +
                        "  \"PickleStepDefinitionMatchTwo_Pattern\" in PickleStepDefinitionMatchTwo_Location"))),
            () -> assertThat(expectedThrown.getCause(), is(nullValue())),
            () -> assertThat(expectedThrown.getMatches(), is(equalTo(matches))));
    }

}
