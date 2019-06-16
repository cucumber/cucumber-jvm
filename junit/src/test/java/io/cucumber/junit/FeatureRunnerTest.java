package io.cucumber.junit;

import cucumber.runner.TimeServiceEventBus;
import cucumber.runner.EventBus;
import cucumber.runner.TimeService;
import cucumber.runtime.Backend;
import cucumber.runtime.BackendSupplier;
import cucumber.runtime.RuntimeOptions;
import cucumber.runner.ThreadLocalRunnerSupplier;
import cucumber.runtime.filter.Filters;
import cucumber.runtime.model.CucumberFeature;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.mockito.InOrder;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

public class FeatureRunnerTest {

    private static void assertDescriptionIsPredictable(Description description, Set<Description> descriptions) {
        assertTrue(descriptions.contains(description));
        for (Description each : description.getChildren()) {
            assertDescriptionIsPredictable(each, descriptions);
        }
    }

    private static void assertDescriptionIsUnique(Description description, Set<Description> descriptions) {
        // Note: JUnit uses the the serializable parameter as the unique id when comparing Descriptions
        assertTrue(descriptions.add(description));
        for (Description each : description.getChildren()) {
            assertDescriptionIsUnique(each, descriptions);
        }
    }

    @Test
    public void should_not_create_step_descriptions_by_default() throws Exception {
        CucumberFeature cucumberFeature = TestPickleBuilder.parseFeature("path/test.feature", "" +
            "Feature: feature name\n" +
            "  Background:\n" +
            "    Given background step\n" +
            "  Scenario: A\n" +
            "    Then scenario name\n" +
            "  Scenario: B\n" +
            "    Then scenario name\n" +
            "  Scenario Outline: C\n" +
            "    Then scenario <name>\n" +
            "  Examples:\n" +
            "    | name |\n" +
            "    | C    |\n" +
            "    | D    |\n" +
            "    | E    |\n"

        );

        FeatureRunner runner = createFeatureRunner(cucumberFeature);

        Description feature = runner.getDescription();
        Description scenarioA = feature.getChildren().get(0);
        assertTrue(scenarioA.getChildren().isEmpty());
        Description scenarioB = feature.getChildren().get(1);
        assertTrue(scenarioB.getChildren().isEmpty());
        Description scenarioC0 = feature.getChildren().get(2);
        assertTrue(scenarioC0.getChildren().isEmpty());
        Description scenarioC1 = feature.getChildren().get(3);
        assertTrue(scenarioC1.getChildren().isEmpty());
        Description scenarioC2 = feature.getChildren().get(4);
        assertTrue(scenarioC2.getChildren().isEmpty());
    }

    @Test
    public void should_not_issue_notification_for_steps_by_default_scenario_outline_with_two_examples_table_and_background() throws Throwable {
        CucumberFeature feature = TestPickleBuilder.parseFeature("path/test.feature", "" +
            "Feature: feature name\n" +
            "  Background: background\n" +
            "    Given first step\n" +
            "  Scenario Outline: scenario outline name\n" +
            "    When <x> step\n" +
            "    Then <y> step\n" +
            "    Examples: examples 1 name\n" +
            "      |   x    |   y   |\n" +
            "      | second | third |\n" +
            "      | second | third |\n" +
            "    Examples: examples 2 name\n" +
            "      |   x    |   y   |\n" +
            "      | second | third |\n");

        RunNotifier notifier = runFeatureWithNotifier(feature);

        InOrder order = inOrder(notifier);

        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("scenario outline name(feature name)")));
        order.verify(notifier, times(3)).fireTestAssumptionFailed(argThat(new FailureMatcher("scenario outline name(feature name)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("scenario outline name(feature name)")));
        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("scenario outline name(feature name)")));
        order.verify(notifier, times(3)).fireTestAssumptionFailed(argThat(new FailureMatcher("scenario outline name(feature name)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("scenario outline name(feature name)")));
        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("scenario outline name(feature name)")));
        order.verify(notifier, times(3)).fireTestAssumptionFailed(argThat(new FailureMatcher("scenario outline name(feature name)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("scenario outline name(feature name)")));
    }

    @Test
    public void should_not_issue_notification_for_steps_by_default_two_scenarios_with_background() throws Throwable {
        CucumberFeature feature = TestPickleBuilder.parseFeature("path/test.feature", "" +
            "Feature: feature name\n" +
            "  Background: background\n" +
            "    Given first step\n" +
            "  Scenario: scenario_1 name\n" +
            "    When second step\n" +
            "    Then third step\n" +
            "  Scenario: scenario_2 name\n" +
            "    Then another second step\n");

        RunNotifier notifier = runFeatureWithNotifier(feature);

        InOrder order = inOrder(notifier);

        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("scenario_1 name(feature name)")));
        order.verify(notifier, times(3)).fireTestAssumptionFailed(argThat(new FailureMatcher("scenario_1 name(feature name)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("scenario_1 name(feature name)")));
        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("scenario_2 name(feature name)")));
        order.verify(notifier, times(2)).fireTestAssumptionFailed(argThat(new FailureMatcher("scenario_2 name(feature name)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("scenario_2 name(feature name)")));
    }

    private RunNotifier runFeatureWithNotifier(CucumberFeature cucumberFeature, String... options) throws InitializationError {
        FeatureRunner runner = createFeatureRunner(cucumberFeature, options);
        RunNotifier notifier = mock(RunNotifier.class);
        runner.run(notifier);
        return notifier;
    }

    private FeatureRunner createFeatureRunner(CucumberFeature cucumberFeature, String... options) throws InitializationError {
        JUnitOptions junitOption = new JUnitOptions(false, Arrays.asList(options));
        return createFeatureRunner(cucumberFeature, junitOption);
    }

    private FeatureRunner createFeatureRunner(CucumberFeature cucumberFeature, JUnitOptions junitOption) throws InitializationError {
        final RuntimeOptions runtimeOptions = new RuntimeOptions("");

        final TimeService timeServiceStub = new TimeService() {
            @Override
            public long time() {
                return 0L;
            }

            @Override
            public long timeMillis() {
                return 0L;
            }
        };
        BackendSupplier backendSupplier = new BackendSupplier() {
            @Override
            public Collection<? extends Backend> get() {
                return asList(mock(Backend.class));
            }
        };

        EventBus bus = new TimeServiceEventBus(timeServiceStub);
        Filters filters = new Filters(runtimeOptions);
        ThreadLocalRunnerSupplier runnerSupplier = new ThreadLocalRunnerSupplier(runtimeOptions, bus, backendSupplier);
        return new FeatureRunner(cucumberFeature, filters, runnerSupplier, junitOption);
    }

    @Test
    public void should_populate_descriptions_with_stable_unique_ids() throws Exception {
        CucumberFeature cucumberFeature = TestPickleBuilder.parseFeature("path/test.feature", "" +
            "Feature: feature name\n" +
            "  Background:\n" +
            "    Given background step\n" +
            "  Scenario: A\n" +
            "    Then scenario name\n" +
            "  Scenario: B\n" +
            "    Then scenario name\n" +
            "  Scenario Outline: C\n" +
            "    Then scenario <name>\n" +
            "  Examples:\n" +
            "    | name |\n" +
            "    | C    |\n" +
            "    | D    |\n" +
            "    | E    |\n"

        );

        FeatureRunner runner = createFeatureRunner(cucumberFeature);
        FeatureRunner rerunner = createFeatureRunner(cucumberFeature);

        Set<Description> descriptions = new HashSet<Description>();
        assertDescriptionIsUnique(runner.getDescription(), descriptions);
        assertDescriptionIsPredictable(runner.getDescription(), descriptions);
        assertDescriptionIsPredictable(rerunner.getDescription(), descriptions);

    }

    @Test
    public void step_descriptions_can_be_turned_on() throws Exception {
        CucumberFeature cucumberFeature = TestPickleBuilder.parseFeature("path/test.feature", "" +
            "Feature: feature name\n" +
            "  Background:\n" +
            "    Given background step\n" +
            "  Scenario: A\n" +
            "    Then scenario name\n" +
            "  Scenario: B\n" +
            "    Then scenario name\n" +
            "  Scenario Outline: C\n" +
            "    Then scenario <name>\n" +
            "  Examples:\n" +
            "    | name |\n" +
            "    | C    |\n" +
            "    | D    |\n" +
            "    | E    |\n"

        );

        FeatureRunner runner = createFeatureRunner(cucumberFeature, "--step-notifications");

        Description feature = runner.getDescription();
        Description scenarioA = feature.getChildren().get(0);
        assertEquals(2, scenarioA.getChildren().size());
        Description scenarioB = feature.getChildren().get(1);
        assertEquals(2, scenarioB.getChildren().size());
        Description scenarioC0 = feature.getChildren().get(2);
        assertEquals(2, scenarioC0.getChildren().size());
        Description scenarioC1 = feature.getChildren().get(3);
        assertEquals(2, scenarioC1.getChildren().size());
        Description scenarioC2 = feature.getChildren().get(4);
        assertEquals(2, scenarioC2.getChildren().size());
    }

    @Test
    public void step_notification_can_be_turned_on_scenario_outline_with_two_examples_table_and_background() throws Throwable {
        CucumberFeature feature = TestPickleBuilder.parseFeature("path/test.feature", "" +
            "Feature: feature name\n" +
            "  Background: background\n" +
            "    Given first step\n" +
            "  Scenario Outline: scenario outline name\n" +
            "    When <x> step\n" +
            "    Then <y> step\n" +
            "    Examples: examples 1 name\n" +
            "      |   x    |   y   |\n" +
            "      | second | third |\n" +
            "      | second | third |\n" +
            "    Examples: examples 2 name\n" +
            "      |   x    |   y   |\n" +
            "      | second | third |\n");

        RunNotifier notifier = runFeatureWithNotifier(feature, "--step-notifications");

        InOrder order = inOrder(notifier);

        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("scenario outline name")));
        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("first step(scenario outline name)")));
        order.verify(notifier).fireTestAssumptionFailed(argThat(new FailureMatcher("first step(scenario outline name)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("first step(scenario outline name)")));
        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("second step(scenario outline name)")));
        order.verify(notifier).fireTestAssumptionFailed(argThat(new FailureMatcher("second step(scenario outline name)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("second step(scenario outline name)")));
        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("third step(scenario outline name)")));
        order.verify(notifier).fireTestAssumptionFailed(argThat(new FailureMatcher("third step(scenario outline name)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("third step(scenario outline name)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("scenario outline name")));
        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("scenario outline name")));
        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("first step(scenario outline name)")));
        order.verify(notifier).fireTestAssumptionFailed(argThat(new FailureMatcher("first step(scenario outline name)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("first step(scenario outline name)")));
        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("second step(scenario outline name)")));
        order.verify(notifier).fireTestAssumptionFailed(argThat(new FailureMatcher("second step(scenario outline name)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("second step(scenario outline name)")));
        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("third step(scenario outline name)")));
        order.verify(notifier).fireTestAssumptionFailed(argThat(new FailureMatcher("third step(scenario outline name)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("third step(scenario outline name)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("scenario outline name")));
        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("scenario outline name")));
        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("first step(scenario outline name)")));
        order.verify(notifier).fireTestAssumptionFailed(argThat(new FailureMatcher("first step(scenario outline name)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("first step(scenario outline name)")));
        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("second step(scenario outline name)")));
        order.verify(notifier).fireTestAssumptionFailed(argThat(new FailureMatcher("second step(scenario outline name)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("second step(scenario outline name)")));
        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("third step(scenario outline name)")));
        order.verify(notifier).fireTestAssumptionFailed(argThat(new FailureMatcher("third step(scenario outline name)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("third step(scenario outline name)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("scenario outline name")));
    }

    @Test
    public void step_notification_can_be_turned_on_two_scenarios_with_background() throws Throwable {
        CucumberFeature feature = TestPickleBuilder.parseFeature("path/test.feature", "" +
            "Feature: feature name\n" +
            "  Background: background\n" +
            "    Given first step\n" +
            "  Scenario: scenario_1 name\n" +
            "    When second step\n" +
            "    Then third step\n" +
            "  Scenario: scenario_2 name\n" +
            "    Then another second step\n");

        RunNotifier notifier = runFeatureWithNotifier(feature, "--step-notifications");

        InOrder order = inOrder(notifier);

        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("scenario_1 name")));
        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("first step(scenario_1 name)")));
        order.verify(notifier).fireTestAssumptionFailed(argThat(new FailureMatcher("first step(scenario_1 name)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("first step(scenario_1 name)")));
        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("second step(scenario_1 name)")));
        order.verify(notifier).fireTestAssumptionFailed(argThat(new FailureMatcher("second step(scenario_1 name)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("second step(scenario_1 name)")));
        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("third step(scenario_1 name)")));
        order.verify(notifier).fireTestAssumptionFailed(argThat(new FailureMatcher("third step(scenario_1 name)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("third step(scenario_1 name)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("scenario_1 name")));
        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("scenario_2 name")));
        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("first step(scenario_2 name)")));
        order.verify(notifier).fireTestAssumptionFailed(argThat(new FailureMatcher("first step(scenario_2 name)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("first step(scenario_2 name)")));
        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("another second step(scenario_2 name)")));
        order.verify(notifier).fireTestAssumptionFailed(argThat(new FailureMatcher("another second step(scenario_2 name)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("another second step(scenario_2 name)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("scenario_2 name")));
    }

}

