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

import static io.cucumber.core.plugin.BytesEqualTo.isBytesEqualTo;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;

class RerunFormatterTest {

    @Test
    void should_leave_report_empty_when_exit_code_is_zero() {
        Feature feature = TestFeatureParser.parse("classpath:path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: passed scenario\n" +
                "    Given passed step\n" +
                "  Scenario: skipped scenario\n" +
                "    Given skipped step\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new RerunFormatter(out))
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("passed step"),
                    new StubStepDefinition("skipped step", new TestAbortedException())))
                .build()
                .run();

        assertThat(out, isBytesEqualTo(""));
    }

    @Test
    void should_put_data_in_report_when_exit_code_is_non_zero() {
        Feature feature = TestFeatureParser.parse("classpath:path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: failed scenario\n" +
                "    Given failed step\n" +
                "  Scenario: pending scenario\n" +
                "    Given pending step\n" +
                "  Scenario: undefined scenario\n" +
                "    Given undefined step\n");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(new RerunFormatter(out))
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("failed step", new StubException()),
                    new StubStepDefinition("pending step", new StubPendingException())))
                .build()
                .run();

        assertThat(out, isBytesEqualTo("classpath:path/test.feature:2:4:6\n"));
    }

    @Test
    void should_use_scenario_location_when_scenario_step_fails() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Given first step\n" +
                "    When second step\n" +
                "    Then third step\n");

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

        assertThat(out, isBytesEqualTo("file:path/test.feature:2\n"));
    }

    @Test
    void should_use_scenario_location_when_background_step_fails() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Background: the background\n" +
                "    Given background step\n" +
                "  Scenario: scenario name\n" +
                "    When second step\n" +
                "    Then third step\n");

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

        assertThat(out, isBytesEqualTo("file:path/test.feature:4\n"));
    }

    @Test
    void should_use_example_row_location_when_scenario_outline_fails() {
        Feature feature = TestFeatureParser.parse("classpath:path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario Outline: scenario name\n" +
                "    When executing <row> row\n" +
                "    Then everything is ok\n" +
                "    Examples:\n" +
                "    |  row   |\n" +
                "    | first  |\n" +
                "    | second |");

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

        assertThat(out, isBytesEqualTo("classpath:path/test.feature:8\n"));
    }

    @Test
    void should_use_scenario_location_when_before_hook_fails() {
        Feature feature = TestFeatureParser.parse("classpath:path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Given first step\n" +
                "    When second step\n" +
                "    Then third step\n");

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

        assertThat(out, isBytesEqualTo("classpath:path/test.feature:2\n"));
    }

    @Test
    void should_use_scenario_location_when_after_hook_fails() {
        Feature feature = TestFeatureParser.parse("classpath:path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Given first step\n" +
                "    When second step\n" +
                "    Then third step\n");

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

        assertThat(out, isBytesEqualTo("classpath:path/test.feature:2\n"));
    }

    @Test
    void should_one_entry_for_feature_with_many_failing_scenarios() {
        Feature feature = TestFeatureParser.parse("classpath:path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario 1 name\n" +
                "    When first step\n" +
                "    Then second step\n" +
                "  Scenario: scenario 2 name\n" +
                "    When third step\n" +
                "    Then forth step\n");

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

        assertThat(out, isBytesEqualTo("classpath:path/test.feature:2:5\n"));
    }

    @Test
    void should_one_entry_for_each_failing_feature() {
        Feature feature1 = TestFeatureParser.parse("classpath:path/first.feature", "" +
                "Feature: feature 1 name\n" +
                "  Scenario: scenario 1 name\n" +
                "    When first step\n" +
                "    Then second step\n");
        Feature feature2 = TestFeatureParser.parse("classpath:path/second.feature", "" +
                "Feature: feature 2 name\n" +
                "  Scenario: scenario 2 name\n" +
                "    When third step\n" +
                "    Then forth step\n");

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
            isBytesEqualTo("classpath:path/first.feature:2\nclasspath:path/second.feature:2\n"));
    }

}
