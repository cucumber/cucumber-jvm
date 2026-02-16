package io.cucumber.core.plugin;

import io.cucumber.core.backend.StubHookDefinition;
import io.cucumber.core.backend.StubPendingException;
import io.cucumber.core.backend.StubStepDefinition;
import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.runtime.Runtime;
import io.cucumber.core.runtime.StubBackendSupplier;
import io.cucumber.core.runtime.StubFeatureSupplier;
import org.junit.jupiter.api.Test;
import org.opentest4j.TestAbortedException;

import java.io.ByteArrayOutputStream;

import static io.cucumber.core.plugin.Bytes.bytes;
import static io.cucumber.core.plugin.IsEqualCompressingLineSeparators.equalCompressingLineSeparators;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

class RerunFormatterTest {

    @Test
    void should_leave_report_empty_when_exit_code_is_zero() {
        Feature feature = TestFeatureParser.parse("classpath:path/test.feature", """
                Feature: feature name
                  Scenario: passed scenario
                    Given passed step
                  Scenario: skipped scenario
                    Given skipped step
                """);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new RerunFormatter(out))
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("passed step"),
                    new StubStepDefinition("skipped step", new TestAbortedException())))
                .build()
                .run();

        assertThat(out, bytes(equalTo("")));
    }

    @Test
    void should_put_data_in_report_when_exit_code_is_non_zero() {
        Feature feature = TestFeatureParser.parse("classpath:path/test.feature", """
                Feature: feature name
                  Scenario: failed scenario
                    Given failed step
                  Scenario: pending scenario
                    Given pending step
                  Scenario: undefined scenario
                    Given undefined step
                """);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new RerunFormatter(out))
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("failed step", new StubException()),
                    new StubStepDefinition("pending step", new StubPendingException())))
                .build()
                .run();

        assertThat(out, bytes(equalCompressingLineSeparators("classpath:path/test.feature:2:4:6\n")));
    }

    @Test
    void should_use_scenario_location_when_scenario_step_fails() {
        Feature feature = TestFeatureParser.parse("path/test.feature", """
                Feature: feature name
                  Scenario: scenario name
                    Given first step
                    When second step
                    Then third step
                """);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new RerunFormatter(out))
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("first step"),
                    new StubStepDefinition("second step"),
                    new StubStepDefinition("third step", new StubException())))
                .build()
                .run();

        assertThat(out, bytes(equalCompressingLineSeparators("file:path/test.feature:2\n")));
    }

    @Test
    void should_use_scenario_location_when_background_step_fails() {
        Feature feature = TestFeatureParser.parse("path/test.feature", """
                Feature: feature name
                  Background: the background
                    Given background step
                  Scenario: scenario name
                    When second step
                    Then third step
                """);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new RerunFormatter(out))
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("background step", new StubException()),
                    new StubStepDefinition("second step"),
                    new StubStepDefinition("third step")))
                .build()
                .run();

        assertThat(out, bytes(equalCompressingLineSeparators("file:path/test.feature:4\n")));
    }

    @Test
    void should_use_example_row_location_when_scenario_outline_fails() {
        Feature feature = TestFeatureParser.parse("classpath:path/test.feature", """
                Feature: feature name
                  Scenario Outline: scenario name
                    When executing <row> row
                    Then everything is ok
                    Examples:
                    |  row   |
                    | first  |
                    | second |""");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new RerunFormatter(out))
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("executing first row"),
                    new StubStepDefinition("executing second row", new StubException()),
                    new StubStepDefinition("everything is ok")))
                .build()
                .run();

        assertThat(out, bytes(equalCompressingLineSeparators("classpath:path/test.feature:8\n")));
    }

    @Test
    void should_use_scenario_location_when_before_hook_fails() {
        Feature feature = TestFeatureParser.parse("classpath:path/test.feature", """
                Feature: feature name
                  Scenario: scenario name
                    Given first step
                    When second step
                    Then third step
                """);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new RerunFormatter(out))
                .withBackendSupplier(new StubBackendSupplier(
                    singletonList(new StubHookDefinition(new StubException())),
                    asList(
                        new StubStepDefinition("first step"),
                        new StubStepDefinition("second step"),
                        new StubStepDefinition("third step")),
                    emptyList()))
                .build()
                .run();

        assertThat(out, bytes(equalCompressingLineSeparators("classpath:path/test.feature:2\n")));
    }

    @Test
    void should_use_scenario_location_when_after_hook_fails() {
        Feature feature = TestFeatureParser.parse("classpath:path/test.feature", """
                Feature: feature name
                  Scenario: scenario name
                    Given first step
                    When second step
                    Then third step
                """);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new RerunFormatter(out))
                .withBackendSupplier(new StubBackendSupplier(
                    emptyList(),
                    asList(
                        new StubStepDefinition("first step"),
                        new StubStepDefinition("second step"),
                        new StubStepDefinition("third step")),
                    singletonList(new StubHookDefinition(new StubException()))))
                .build()
                .run();

        assertThat(out, bytes(equalCompressingLineSeparators("classpath:path/test.feature:2\n")));
    }

    @Test
    void should_one_entry_for_feature_with_many_failing_scenarios() {
        Feature feature = TestFeatureParser.parse("classpath:path/test.feature", """
                Feature: feature name
                  Scenario: scenario 1 name
                    When first step
                    Then second step
                  Scenario: scenario 2 name
                    When third step
                    Then forth step
                """);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new RerunFormatter(out))
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("first step"),
                    new StubStepDefinition("second step", new StubException()),
                    new StubStepDefinition("third step", new StubException()),
                    new StubStepDefinition("forth step")))
                .build()
                .run();

        assertThat(out, bytes(equalCompressingLineSeparators("classpath:path/test.feature:2:5\n")));
    }

    @Test
    void should_one_entry_for_each_failing_feature() {
        Feature feature1 = TestFeatureParser.parse("classpath:path/first.feature", """
                Feature: feature 1 name
                  Scenario: scenario 1 name
                    When first step
                    Then second step
                """);
        Feature feature2 = TestFeatureParser.parse("classpath:path/second.feature", """
                Feature: feature 2 name
                  Scenario: scenario 2 name
                    When third step
                    Then forth step
                """);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature1, feature2))
                .withAdditionalPlugins(new RerunFormatter(out))
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("first step"),
                    new StubStepDefinition("second step", new StubException()),
                    new StubStepDefinition("third step", new StubException()),
                    new StubStepDefinition("forth step")))
                .build()
                .run();

        assertThat(out,
            bytes(
                equalCompressingLineSeparators("classpath:path/first.feature:2\nclasspath:path/second.feature:2\n")));
    }

}
