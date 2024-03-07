package io.cucumber.junit;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.filter.Filters;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.options.RuntimeOptionsBuilder;
import io.cucumber.core.runtime.BackendSupplier;
import io.cucumber.core.runtime.CucumberExecutionContext;
import io.cucumber.core.runtime.ExitStatus;
import io.cucumber.core.runtime.ObjectFactoryServiceLoader;
import io.cucumber.core.runtime.ObjectFactorySupplier;
import io.cucumber.core.runtime.RunnerSupplier;
import io.cucumber.core.runtime.SingletonObjectFactorySupplier;
import io.cucumber.core.runtime.ThreadLocalRunnerSupplier;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.tagexpressions.TagExpressionParser;
import org.junit.jupiter.api.Test;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.Collections.singleton;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeatureRunnerTest {

    private static void assertDescriptionIsPredictable(Description description, Set<Description> descriptions) {
        assertTrue(descriptions.contains(description));
        for (Description each : description.getChildren()) {
            assertDescriptionIsPredictable(each, descriptions);
        }
    }

    private static void assertDescriptionIsUnique(Description description, Set<Description> descriptions) {
        // Note: JUnit uses the the serializable parameter as the unique id when
        // comparing Descriptions
        assertTrue(descriptions.add(description));
        for (Description each : description.getChildren()) {
            assertDescriptionIsUnique(each, descriptions);
        }
    }

    @Test
    void should_not_create_step_descriptions_by_default() {
        Feature cucumberFeature = TestPickleBuilder.parseFeature("path/test.feature", "" +
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

        FeatureRunner runner = createFeatureRunner(cucumberFeature, new JUnitOptions());

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

    private FeatureRunner createFeatureRunner(Feature feature, JUnitOptions junitOption) {
        ObjectFactoryServiceLoader objectFactoryServiceLoader = new ObjectFactoryServiceLoader(
            getClass()::getClassLoader, RuntimeOptions.defaultOptions());
        ObjectFactorySupplier objectFactory = new SingletonObjectFactorySupplier(objectFactoryServiceLoader);
        final RuntimeOptions runtimeOptions = RuntimeOptions.defaultOptions();

        final Clock clockStub = new Clock() {
            @Override
            public ZoneId getZone() {
                return null;
            }

            @Override
            public Clock withZone(ZoneId zone) {
                return null;
            }

            @Override
            public Instant instant() {
                return Instant.EPOCH;
            }
        };
        BackendSupplier backendSupplier = () -> singleton(new StubBackendProviderService.StubBackend());

        EventBus bus = new TimeServiceEventBus(clockStub, UUID::randomUUID);
        Filters filters = new Filters(runtimeOptions);
        ThreadLocalRunnerSupplier runnerSupplier = new ThreadLocalRunnerSupplier(runtimeOptions, bus, backendSupplier,
            objectFactory);
        CucumberExecutionContext context = new CucumberExecutionContext(bus, new ExitStatus(runtimeOptions),
            runnerSupplier);
        return FeatureRunner.create(feature, null, filters, context, junitOption);
    }

    @Test
    void should_not_issue_notification_for_steps_by_default_scenario_outline_with_two_examples_table_and_background() {
        Feature feature = TestPickleBuilder.parseFeature("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Background: background\n" +
                "    Given step #1\n" +
                "  Scenario Outline: scenario <id>\n" +
                "    When step #2 \n" +
                "    Then step #3 \n" +
                "    Examples: examples 1 name\n" +
                "      | id | \n" +
                "      | #1 |\n" +
                "      | #2  |\n" +
                "    Examples: examples 2 name\n" +
                "      | id |\n" +
                "      | #3 |\n");
        MockRunNotifier notifier = runFeatureWithNotifier(feature, new JUnitOptions());

        assertIterableEquals(
            List.of(
                new RunNotifierEvent("fireTestStarted", "scenario #1(feature name)"),
                new RunNotifierEvent("fireTestFailure", "scenario #1(feature name)"),
                new RunNotifierEvent("fireTestFinished", "scenario #1(feature name)"),
                new RunNotifierEvent("fireTestStarted", "scenario #2(feature name)"),
                new RunNotifierEvent("fireTestFailure", "scenario #2(feature name)"),
                new RunNotifierEvent("fireTestFinished", "scenario #2(feature name)"),
                new RunNotifierEvent("fireTestStarted", "scenario #3(feature name)"),
                new RunNotifierEvent("fireTestFailure", "scenario #3(feature name)"),
                new RunNotifierEvent("fireTestFinished", "scenario #3(feature name)")),
            notifier.events);
    }

    private MockRunNotifier runFeatureWithNotifier(Feature feature, JUnitOptions options) {
        FeatureRunner runner = createFeatureRunner(feature, options);
        MockRunNotifier notifier = new MockRunNotifier();
        runner.run(notifier);
        return notifier;
    }

    @Test
    void should_not_issue_notification_for_steps_by_default_two_scenarios_with_background() {
        Feature feature = TestPickleBuilder.parseFeature("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Background: background\n" +
                "    Given step #1\n" +
                "  Scenario: scenario_1 name\n" +
                "    When step #2\n" +
                "    Then step #3\n" +
                "  Scenario: scenario_2 name\n" +
                "    Then step #2\n");

        MockRunNotifier notifier = runFeatureWithNotifier(feature, new JUnitOptions());

        assertIterableEquals(
            List.of(
                new RunNotifierEvent("fireTestStarted", "scenario_1 name(feature name)"),
                new RunNotifierEvent("fireTestFailure", "scenario_1 name(feature name)"),
                new RunNotifierEvent("fireTestFinished", "scenario_1 name(feature name)"),
                new RunNotifierEvent("fireTestStarted", "scenario_2 name(feature name)"),
                new RunNotifierEvent("fireTestFailure", "scenario_2 name(feature name)"),
                new RunNotifierEvent("fireTestFinished", "scenario_2 name(feature name)")),
            notifier.events);
    }

    @Test
    void should_populate_descriptions_with_stable_unique_ids() {
        Feature feature = TestPickleBuilder.parseFeature("path/test.feature", "" +
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

        FeatureRunner runner = createFeatureRunner(feature, new JUnitOptions());
        FeatureRunner rerunner = createFeatureRunner(feature, new JUnitOptions());

        Set<Description> descriptions = new HashSet<>();
        assertDescriptionIsUnique(runner.getDescription(), descriptions);
        assertDescriptionIsPredictable(runner.getDescription(), descriptions);
        assertDescriptionIsPredictable(rerunner.getDescription(), descriptions);

    }

    @Test
    void step_descriptions_can_be_turned_on() {
        Feature cucumberFeature = TestPickleBuilder.parseFeature("path/test.feature", "" +
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

        JUnitOptions junitOption = new JUnitOptionsBuilder().setStepNotifications(true).build();
        FeatureRunner runner = createFeatureRunner(cucumberFeature, junitOption);

        Description feature = runner.getDescription();
        Description scenarioA = feature.getChildren().get(0);
        assertThat(scenarioA.getChildren().size(), is(equalTo(2)));
        Description scenarioB = feature.getChildren().get(1);
        assertThat(scenarioB.getChildren().size(), is(equalTo(2)));
        Description scenarioC0 = feature.getChildren().get(2);
        assertThat(scenarioC0.getChildren().size(), is(equalTo(2)));
        Description scenarioC1 = feature.getChildren().get(3);
        assertThat(scenarioC1.getChildren().size(), is(equalTo(2)));
        Description scenarioC2 = feature.getChildren().get(4);
        assertThat(scenarioC2.getChildren().size(), is(equalTo(2)));
    }

    @Test
    void step_notification_can_be_turned_on_scenario_outline_with_two_examples_table_and_background() {
        Feature feature = TestPickleBuilder.parseFeature("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Background: background\n" +
                "    Given step #1\n" +
                "  Scenario Outline: scenario <id>\n" +
                "    When step #2 \n" +
                "    Then step #3 \n" +
                "    Examples: examples 1 name\n" +
                "      | id | \n" +
                "      | #1 |\n" +
                "      | #2  |\n" +
                "    Examples: examples 2 name\n" +
                "      | id |\n" +
                "      | #3 |\n");

        JUnitOptions junitOption = new JUnitOptionsBuilder().setStepNotifications(true).build();
        MockRunNotifier notifier = runFeatureWithNotifier(feature, junitOption);

        assertIterableEquals(
            List.of(
                new RunNotifierEvent("fireTestStarted", "scenario #1"),
                new RunNotifierEvent("fireTestStarted", "step #1(scenario #1)"),
                new RunNotifierEvent("fireTestFailure", "step #1(scenario #1)"),
                new RunNotifierEvent("fireTestFinished", "step #1(scenario #1)"),
                new RunNotifierEvent("fireTestStarted", "step #2(scenario #1)"),
                new RunNotifierEvent("fireTestAssumptionFailed", "step #2(scenario #1)"),
                new RunNotifierEvent("fireTestFinished", "step #2(scenario #1)"),
                new RunNotifierEvent("fireTestStarted", "step #3(scenario #1)"),
                new RunNotifierEvent("fireTestAssumptionFailed", "step #3(scenario #1)"),
                new RunNotifierEvent("fireTestFinished", "step #3(scenario #1)"),
                new RunNotifierEvent("fireTestFailure", "scenario #1"),
                new RunNotifierEvent("fireTestFinished", "scenario #1"),

                new RunNotifierEvent("fireTestStarted", "scenario #2"),
                new RunNotifierEvent("fireTestStarted", "step #1(scenario #2)"),
                new RunNotifierEvent("fireTestFailure", "step #1(scenario #2)"),
                new RunNotifierEvent("fireTestFinished", "step #1(scenario #2)"),
                new RunNotifierEvent("fireTestStarted", "step #2(scenario #2)"),
                new RunNotifierEvent("fireTestAssumptionFailed", "step #2(scenario #2)"),
                new RunNotifierEvent("fireTestFinished", "step #2(scenario #2)"),
                new RunNotifierEvent("fireTestStarted", "step #3(scenario #2)"),
                new RunNotifierEvent("fireTestAssumptionFailed", "step #3(scenario #2)"),
                new RunNotifierEvent("fireTestFinished", "step #3(scenario #2)"),
                new RunNotifierEvent("fireTestFailure", "scenario #2"),
                new RunNotifierEvent("fireTestFinished", "scenario #2"),

                new RunNotifierEvent("fireTestStarted", "scenario #3"),
                new RunNotifierEvent("fireTestStarted", "step #1(scenario #3)"),
                new RunNotifierEvent("fireTestFailure", "step #1(scenario #3)"),
                new RunNotifierEvent("fireTestFinished", "step #1(scenario #3)"),
                new RunNotifierEvent("fireTestStarted", "step #2(scenario #3)"),
                new RunNotifierEvent("fireTestAssumptionFailed", "step #2(scenario #3)"),
                new RunNotifierEvent("fireTestFinished", "step #2(scenario #3)"),
                new RunNotifierEvent("fireTestStarted", "step #3(scenario #3)"),
                new RunNotifierEvent("fireTestAssumptionFailed", "step #3(scenario #3)"),
                new RunNotifierEvent("fireTestFinished", "step #3(scenario #3)"),
                new RunNotifierEvent("fireTestFailure", "scenario #3"),
                new RunNotifierEvent("fireTestFinished", "scenario #3")),
            notifier.events);
    }

    @Test
    void step_notification_can_be_turned_on_two_scenarios_with_background() {
        Feature feature = TestPickleBuilder.parseFeature("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Background: background\n" +
                "    Given step #1\n" +
                "  Scenario: scenario_1 name\n" +
                "    When step #2\n" +
                "    Then step #3\n" +
                "  Scenario: scenario_2 name\n" +
                "    Then another step #2\n");

        JUnitOptions junitOption = new JUnitOptionsBuilder().setStepNotifications(true).build();
        MockRunNotifier notifier = runFeatureWithNotifier(feature, junitOption);

        assertIterableEquals(
            List.of(
                new RunNotifierEvent("fireTestStarted", "scenario_1 name"),
                new RunNotifierEvent("fireTestStarted", "step #1(scenario_1 name)"),
                new RunNotifierEvent("fireTestFailure", "step #1(scenario_1 name)"),
                new RunNotifierEvent("fireTestFinished", "step #1(scenario_1 name)"),
                new RunNotifierEvent("fireTestStarted", "step #2(scenario_1 name)"),
                new RunNotifierEvent("fireTestAssumptionFailed", "step #2(scenario_1 name)"),
                new RunNotifierEvent("fireTestFinished", "step #2(scenario_1 name)"),
                new RunNotifierEvent("fireTestStarted", "step #3(scenario_1 name)"),
                new RunNotifierEvent("fireTestAssumptionFailed", "step #3(scenario_1 name)"),
                new RunNotifierEvent("fireTestFinished", "step #3(scenario_1 name)"),
                new RunNotifierEvent("fireTestFailure", "scenario_1 name"),
                new RunNotifierEvent("fireTestFinished", "scenario_1 name"),

                new RunNotifierEvent("fireTestStarted", "scenario_2 name"),
                new RunNotifierEvent("fireTestStarted", "step #1(scenario_2 name)"),
                new RunNotifierEvent("fireTestFailure", "step #1(scenario_2 name)"),
                new RunNotifierEvent("fireTestFinished", "step #1(scenario_2 name)"),
                new RunNotifierEvent("fireTestStarted", "another step #2(scenario_2 name)"),
                new RunNotifierEvent("fireTestAssumptionFailed", "another step #2(scenario_2 name)"),
                new RunNotifierEvent("fireTestFinished", "another step #2(scenario_2 name)"),
                new RunNotifierEvent("fireTestFailure", "scenario_2 name"),
                new RunNotifierEvent("fireTestFinished", "scenario_2 name")),
            notifier.events);
    }

    @Test
    void should_notify_of_failure_to_create_runners_and_request_test_execution_to_stop() {
        Feature feature = TestPickleBuilder.parseFeature("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario_1 name\n" +
                "    Given step #1\n");

        Filters filters = new Filters(RuntimeOptions.defaultOptions());

        IllegalStateException illegalStateException = new IllegalStateException();
        RunnerSupplier runnerSupplier = () -> {
            throw illegalStateException;
        };
        TimeServiceEventBus bus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);
        RuntimeOptions options = RuntimeOptions.defaultOptions();
        CucumberExecutionContext context = new CucumberExecutionContext(bus, new ExitStatus(options), runnerSupplier);
        FeatureRunner featureRunner = FeatureRunner.create(feature, null, filters, context, new JUnitOptions());

        MockRunNotifier notifier = new MockRunNotifier();
        PickleRunners.PickleRunner pickleRunner = featureRunner.getChildren().get(0);
        featureRunner.runChild(pickleRunner, notifier);

        Description description = pickleRunner.getDescription();
        assertIterableEquals(
            List.of(
                new RunNotifierEvent("fireTestStarted", description.getDisplayName()),
                new RunNotifierEvent("fireTestFailure", description.getDisplayName()),
                new RunNotifierEvent("pleaseStop", ""),
                new RunNotifierEvent("fireTestFinished", description.getDisplayName())),
            notifier.events);
        assertEquals(illegalStateException, notifier.events.get(1).throwable);
    }

    @Test
    void should_filter_pickles() {
        Feature feature = TestPickleBuilder.parseFeature("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: scenario_1 name\n" +
                "    Given step #1\n" +
                "  @tag\n" +
                "  Scenario: scenario_2 name\n" +
                "    Given step #1\n"

        );

        RuntimeOptions options = new RuntimeOptionsBuilder()
                .addTagFilter(TagExpressionParser.parse("@tag"))
                .build();
        Filters filters = new Filters(options);

        IllegalStateException illegalStateException = new IllegalStateException();
        RunnerSupplier runnerSupplier = () -> {
            throw illegalStateException;
        };

        EventBus bus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);
        CucumberExecutionContext context = new CucumberExecutionContext(bus, new ExitStatus(options), runnerSupplier);
        FeatureRunner featureRunner = FeatureRunner.create(feature, null, filters, context, new JUnitOptions());
        assertThat(featureRunner.getChildren().size(), is(1));
        assertThat(featureRunner.getChildren().get(0).getDescription().getDisplayName(),
            is("scenario_2 name(feature name)"));
    }

    private static class MockRunNotifier extends RunNotifier {
        List<RunNotifierEvent> events = new ArrayList<>();

        @Override
        public void fireTestStarted(Description description) throws StoppedByUserException {
            this.events.add(new RunNotifierEvent("fireTestStarted", description.getDisplayName()));
        }

        @Override
        public void fireTestFailure(Failure failure) {
            this.events.add(new RunNotifierEvent("fireTestFailure", failure.getDescription().getDisplayName(),
                failure.getException()));
        }

        @Override
        public void fireTestAssumptionFailed(Failure failure) {
            this.events.add(new RunNotifierEvent("fireTestAssumptionFailed", failure.getDescription().getDisplayName(),
                failure.getException()));
        }

        @Override
        public void fireTestFinished(Description description) {
            this.events.add(new RunNotifierEvent("fireTestFinished", description.getDisplayName()));
        }

        @Override
        public void pleaseStop() {
            this.events.add(new RunNotifierEvent("pleaseStop", ""));
        }

    }

    private static class RunNotifierEvent {
        private final String eventName;
        private final String description;
        private final Throwable throwable;
        public RunNotifierEvent(String eventName, String description) {
            this.eventName = eventName;
            this.description = description;
            this.throwable = null;
        }

        public RunNotifierEvent(String eventName, String description, Throwable throwable) {
            this.eventName = eventName;
            this.description = description;
            this.throwable = throwable;
        }

        public boolean equals(Object o) {
            if (o instanceof RunNotifierEvent) {
                return this.toString().equals(o.toString());
            } else {
                return false;
            }
        }

        public String toString() {
            return eventName + ", description=" + description;
        }
    }
}
