package io.cucumber.core.runner;

import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.backend.StubStepDefinition;
import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Step;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertAll;

class AmbiguousStepDefinitionsExceptionTest {

    @Test
    void can_report_ambiguous_step_definitions() {
        Feature feature = TestFeatureParser.parse("" +
                "Feature: Test feature\n" +
                "  Scenario: Test scenario\n" +
                "     Given I have 4 cukes in my belly\n");

        Step mockPickleStep = feature.getPickles().get(0).getSteps().get(0);

        PickleStepDefinitionMatch mockPickleStepDefinitionMatchOne = new PickleStepDefinitionMatch(new ArrayList<>(),
            new StubStepDefinition("PickleStepDefinitionMatchOne_Pattern", "PickleStepDefinitionMatchOne_Location"),
            null, null);

        PickleStepDefinitionMatch mockPickleStepDefinitionMatchTwo = new PickleStepDefinitionMatch(new ArrayList<>(),
            new StubStepDefinition("PickleStepDefinitionMatchTwo_Pattern", "PickleStepDefinitionMatchTwo_Location"),
            null, null);

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
