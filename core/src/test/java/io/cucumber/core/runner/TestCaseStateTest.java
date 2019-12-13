package io.cucumber.core.runner;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.gherkin.Feature;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Collections;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;

class TestCaseStateTest {

    @Test
    void provides_the_uri_of_the_feature_file() {
        Feature feature = TestFeatureParser.parse("file:path/file.feature", "" +
            "Feature: Test feature\n" +
            "  Scenario: Test scenario\n" +
            "     Given I have 4 cukes in my belly\n"
        );
        TestCaseState state = createTestCaseState(feature);
        assertThat(state.getUri(), is(new File("path/file.feature").toURI()));
    }

    @Test
    void provides_the_scenario_line() {
        Feature feature = TestFeatureParser.parse("" +
            "Feature: Test feature\n" +
            "  Scenario: Test scenario\n" +
            "     Given I have 4 cukes in my belly\n"
        );

        TestCaseState state = createTestCaseState(feature);
        assertThat(state.getLine(), is(2));
    }

    @Test
    void provides_both_the_example_row_line_and_scenario_outline_line_for_scenarios_from_scenario_outlines() {
        Feature feature = TestFeatureParser.parse("" +
            "Feature: Test feature\n" +
            "  Scenario Outline: Test scenario\n" +
            "     Given I have 4 <thing> in my belly\n" +
            "     Examples:\n" +
            "       | thing | \n" +
            "       | cuke  | \n"
        );

        TestCaseState state = createTestCaseState(feature);
        assertThat(state.getLine(), is(6));
    }

    @Test
    void provides_the_uri_and_scenario_line_as_unique_id() {
        Feature feature = TestFeatureParser.parse("file:path/file.feature", "" +
            "Feature: Test feature\n" +
            "  Scenario: Test scenario\n" +
            "     Given I have 4 cukes in my belly\n"
        );

        TestCaseState state = createTestCaseState(feature);

        assertThat(state.getId(), is(new File("path/file.feature:2").toURI().toString()));
    }

    @Test
    void provides_the_uri_and_example_row_line_as_unique_id_for_scenarios_from_scenario_outlines() {
        Feature feature = TestFeatureParser.parse("file:path/file.feature", "" +
            "Feature: Test feature\n" +
            "  Scenario Outline: Test scenario\n" +
            "     Given I have 4 <thing> in my belly\n" +
            "     Examples:\n" +
            "       | thing | \n" +
            "       | cuke  | \n"
        );
        TestCaseState state = createTestCaseState(feature);

        assertThat(state.getId(), is(new File("path/file.feature:6").toURI().toString()));
    }

    private TestCaseState createTestCaseState(Feature feature) {
        return new TestCaseState(mock(EventBus.class), new TestCase(
            UUID.randomUUID(),
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            feature.getPickles().get(0),
            false
        ));
    }

}
