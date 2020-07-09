package io.cucumber.junit;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.filter.Filters;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.options.RuntimeOptionsBuilder;
import io.cucumber.core.runtime.BackendSupplier;
import io.cucumber.core.runtime.ObjectFactoryServiceLoader;
import io.cucumber.core.runtime.ObjectFactorySupplier;
import io.cucumber.core.runtime.RunnerSupplier;
import io.cucumber.core.runtime.ScanningTypeRegistryConfigurerSupplier;
import io.cucumber.core.runtime.SingletonObjectFactorySupplier;
import io.cucumber.core.runtime.ThreadLocalRunnerSupplier;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.tagexpressions.TagExpressionParser;
import org.junit.jupiter.api.Test;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import static java.util.Collections.singleton;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

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
            RuntimeOptions.defaultOptions());
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
        Supplier<ClassLoader> classLoader = FeatureRunnerTest.class::getClassLoader;
        ScanningTypeRegistryConfigurerSupplier typeRegistrySupplier = new ScanningTypeRegistryConfigurerSupplier(
            classLoader, runtimeOptions);
        ThreadLocalRunnerSupplier runnerSupplier = new ThreadLocalRunnerSupplier(runtimeOptions, bus, backendSupplier,
            objectFactory, typeRegistrySupplier);
        return FeatureRunner.create(feature, null, filters, runnerSupplier, junitOption);
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
        RunNotifier notifier = runFeatureWithNotifier(feature, new JUnitOptions());

        InOrder order = inOrder(notifier);

        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("scenario #1(feature name)")));
        order.verify(notifier, times(1)).fireTestFailure(argThat(new FailureMatcher("scenario #1(feature name)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("scenario #1(feature name)")));
        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("scenario #2(feature name)")));
        order.verify(notifier, times(1)).fireTestFailure(argThat(new FailureMatcher("scenario #2(feature name)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("scenario #2(feature name)")));
        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("scenario #3(feature name)")));
        order.verify(notifier, times(1)).fireTestFailure(argThat(new FailureMatcher("scenario #3(feature name)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("scenario #3(feature name)")));
    }

    private RunNotifier runFeatureWithNotifier(Feature feature, JUnitOptions options) {
        FeatureRunner runner = createFeatureRunner(feature, options);
        RunNotifier notifier = mock(RunNotifier.class);
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

        RunNotifier notifier = runFeatureWithNotifier(feature, new JUnitOptions());

        InOrder order = inOrder(notifier);

        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("scenario_1 name(feature name)")));
        order.verify(notifier, times(1)).fireTestFailure(argThat(new FailureMatcher("scenario_1 name(feature name)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("scenario_1 name(feature name)")));
        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("scenario_2 name(feature name)")));
        order.verify(notifier, times(1)).fireTestFailure(argThat(new FailureMatcher("scenario_2 name(feature name)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("scenario_2 name(feature name)")));
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
        RunNotifier notifier = runFeatureWithNotifier(feature, junitOption);

        InOrder order = inOrder(notifier);

        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("scenario #1")));
        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("step #1(scenario #1)")));
        order.verify(notifier).fireTestFailure(argThat(new FailureMatcher("step #1(scenario #1)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("step #1(scenario #1)")));
        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("step #2(scenario #1)")));
        order.verify(notifier).fireTestAssumptionFailed(argThat(new FailureMatcher("step #2(scenario #1)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("step #2(scenario #1)")));
        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("step #3(scenario #1)")));
        order.verify(notifier).fireTestAssumptionFailed(argThat(new FailureMatcher("step #3(scenario #1)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("step #3(scenario #1)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("scenario #1")));

        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("scenario #2")));
        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("step #1(scenario #2)")));
        order.verify(notifier).fireTestFailure(argThat(new FailureMatcher("step #1(scenario #2)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("step #1(scenario #2)")));
        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("step #2(scenario #2)")));
        order.verify(notifier).fireTestAssumptionFailed(argThat(new FailureMatcher("step #2(scenario #2)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("step #2(scenario #2)")));
        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("step #3(scenario #2)")));
        order.verify(notifier).fireTestAssumptionFailed(argThat(new FailureMatcher("step #3(scenario #2)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("step #3(scenario #2)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("scenario #2")));

        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("scenario #3")));
        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("step #1(scenario #3)")));
        order.verify(notifier).fireTestFailure(argThat(new FailureMatcher("step #1(scenario #3)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("step #1(scenario #3)")));
        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("step #2(scenario #3)")));
        order.verify(notifier).fireTestAssumptionFailed(argThat(new FailureMatcher("step #2(scenario #3)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("step #2(scenario #3)")));
        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("step #3(scenario #3)")));
        order.verify(notifier).fireTestAssumptionFailed(argThat(new FailureMatcher("step #3(scenario #3)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("step #3(scenario #3)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("scenario #3")));
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
        RunNotifier notifier = runFeatureWithNotifier(feature, junitOption);

        InOrder order = inOrder(notifier);

        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("scenario_1 name")));
        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("step #1(scenario_1 name)")));
        order.verify(notifier).fireTestFailure(argThat(new FailureMatcher("step #1(scenario_1 name)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("step #1(scenario_1 name)")));
        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("step #2(scenario_1 name)")));
        order.verify(notifier).fireTestAssumptionFailed(argThat(new FailureMatcher("step #2(scenario_1 name)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("step #2(scenario_1 name)")));
        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("step #3(scenario_1 name)")));
        order.verify(notifier).fireTestAssumptionFailed(argThat(new FailureMatcher("step #3(scenario_1 name)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("step #3(scenario_1 name)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("scenario_1 name")));

        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("scenario_2 name")));
        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("step #1(scenario_2 name)")));
        order.verify(notifier).fireTestFailure(argThat(new FailureMatcher("step #1(scenario_2 name)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("step #1(scenario_2 name)")));
        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("another step #2(scenario_2 name)")));
        order.verify(notifier)
                .fireTestAssumptionFailed(argThat(new FailureMatcher("another step #2(scenario_2 name)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("another step #2(scenario_2 name)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("scenario_2 name")));
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

        FeatureRunner featureRunner = FeatureRunner.create(feature, null, filters, runnerSupplier, new JUnitOptions());

        RunNotifier notifier = mock(RunNotifier.class);
        PickleRunners.PickleRunner pickleRunner = featureRunner.getChildren().get(0);
        featureRunner.runChild(pickleRunner, notifier);

        Description description = pickleRunner.getDescription();
        ArgumentCaptor<Failure> failureArgumentCaptor = ArgumentCaptor.forClass(Failure.class);

        InOrder order = inOrder(notifier);
        order.verify(notifier).fireTestStarted(description);
        order.verify(notifier).fireTestFailure(failureArgumentCaptor.capture());
        assertThat(failureArgumentCaptor.getValue().getException(), is(equalTo(illegalStateException)));
        assertThat(failureArgumentCaptor.getValue().getDescription(), is(equalTo(description)));
        order.verify(notifier).pleaseStop();
        order.verify(notifier).fireTestFinished(description);
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

        FeatureRunner featureRunner = FeatureRunner.create(feature, null, filters, runnerSupplier, new JUnitOptions());
        assertThat(featureRunner.getChildren().size(), is(1));
        assertThat(featureRunner.getChildren().get(0).getDescription().getDisplayName(),
            is("scenario_2 name(feature name)"));
    }

}
