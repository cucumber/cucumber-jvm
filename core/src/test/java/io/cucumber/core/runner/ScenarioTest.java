package io.cucumber.core.runner;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.feature.CucumberFeature;
import io.cucumber.core.feature.TestFeatureParser;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.mock;

class ScenarioTest {

    @Test
    void provides_the_uri_of_the_feature_file() {
        CucumberFeature feature = TestFeatureParser.parse("file:path/file.feature", "" +
            "Feature: Test feature\n" +
            "  Scenario: Test scenario\n" +
            "     Given I have 4 cukes in my belly\n"
        );
        Scenario scenario = createScenario(feature);
        assertThat(scenario.getUri(), is(equalTo("file:path/file.feature")));
    }

    @Test
    void provides_the_scenario_line() {
        CucumberFeature feature = TestFeatureParser.parse("" +
            "Feature: Test feature\n" +
            "  Scenario: Test scenario\n" +
            "     Given I have 4 cukes in my belly\n"
        );

        Scenario scenario = createScenario(feature);
        assertThat(scenario.getLine(), is(equalTo(2)));
    }

    @Test
    void provides_both_the_example_row_line_and_scenario_outline_line_for_scenarios_from_scenario_outlines() {
        CucumberFeature feature = TestFeatureParser.parse("" +
            "Feature: Test feature\n" +
            "  Scenario Outline: Test scenario\n" +
            "     Given I have 4 <thing> in my belly\n" +
            "     Examples:\n" +
            "       | thing | \n" +
            "       | cuke  | \n"
        );

        Scenario scenario = createScenario(feature);
        assertThat(scenario.getLine(), is(equalTo(6)));
    }

    @Test
    void provides_the_uri_and_scenario_line_as_unique_id() {
        CucumberFeature feature = TestFeatureParser.parse("file:path/file.feature", "" +
            "Feature: Test feature\n" +
            "  Scenario: Test scenario\n" +
            "     Given I have 4 cukes in my belly\n"
        );

        Scenario scenario = createScenario(feature);

        assertThat(scenario.getId(), is(equalTo("file:path/file.feature:2")));
    }

    @Test
    void provides_the_uri_and_example_row_line_as_unique_id_for_scenarios_from_scenario_outlines() {
        CucumberFeature feature = TestFeatureParser.parse("file:path/file.feature", "" +
            "Feature: Test feature\n" +
            "  Scenario Outline: Test scenario\n" +
            "     Given I have 4 <thing> in my belly\n" +
            "     Examples:\n" +
            "       | thing | \n" +
            "       | cuke  | \n"
        );
        Scenario scenario = createScenario(feature);

        assertThat(scenario.getId(), is(equalTo("file:path/file.feature:6")));
    }

    private Scenario createScenario(CucumberFeature feature) {
        return new Scenario(mock(EventBus.class), new TestCase(
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            feature.getPickles().get(0),
            false
        ));
    }

}
