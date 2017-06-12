package cucumber.runtime.junit;

import cucumber.runner.TimeService;
import cucumber.runtime.Backend;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeGlue;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.io.ClasspathResourceLoader;
import cucumber.runtime.model.CucumberFeature;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.mockito.ArgumentMatcher;
import org.mockito.InOrder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

public class FeatureRunnerTest {

    @Test
    public void should_call_formatter_for_two_scenarios_with_background() throws Throwable {
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

        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("scenario_1 name")));
        order.verify(notifier).fireTestAssumptionFailed(argThat(new FailureMatcher("first step(scenario_1 name)")));
        order.verify(notifier).fireTestAssumptionFailed(argThat(new FailureMatcher("second step(scenario_1 name)")));
        order.verify(notifier).fireTestAssumptionFailed(argThat(new FailureMatcher("third step(scenario_1 name)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("scenario_1 name")));
        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("scenario_2 name")));
        order.verify(notifier).fireTestAssumptionFailed(argThat(new FailureMatcher("first step(scenario_2 name)")));
        order.verify(notifier).fireTestAssumptionFailed(argThat(new FailureMatcher("another second step(scenario_2 name)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("scenario_2 name")));
    }

    @Test
    public void should_call_formatter_for_scenario_outline_with_two_examples_table_and_background() throws Throwable {
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

        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("scenario outline name")));
        order.verify(notifier).fireTestAssumptionFailed(argThat(new FailureMatcher("first step(scenario outline name)")));
        order.verify(notifier).fireTestAssumptionFailed(argThat(new FailureMatcher("second step(scenario outline name)")));
        order.verify(notifier).fireTestAssumptionFailed(argThat(new FailureMatcher("third step(scenario outline name)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("scenario outline name")));
        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("scenario outline name")));
        order.verify(notifier).fireTestAssumptionFailed(argThat(new FailureMatcher("first step(scenario outline name)")));
        order.verify(notifier).fireTestAssumptionFailed(argThat(new FailureMatcher("second step(scenario outline name)")));
        order.verify(notifier).fireTestAssumptionFailed(argThat(new FailureMatcher("third step(scenario outline name)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("scenario outline name")));
        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("scenario outline name")));
        order.verify(notifier).fireTestAssumptionFailed(argThat(new FailureMatcher("first step(scenario outline name)")));
        order.verify(notifier).fireTestAssumptionFailed(argThat(new FailureMatcher("second step(scenario outline name)")));
        order.verify(notifier).fireTestAssumptionFailed(argThat(new FailureMatcher("third step(scenario outline name)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("scenario outline name")));
    }

    private RunNotifier runFeatureWithNotifier(CucumberFeature cucumberFeature) throws InitializationError {
        FeatureRunner runner = createFeatureRunner(cucumberFeature);
        RunNotifier notifier = mock(RunNotifier.class);
        runner.run(notifier);
        return notifier;
    }

    private FeatureRunner createFeatureRunner(CucumberFeature cucumberFeature, String... options) throws InitializationError {
        JUnitOptions junitOption = new JUnitOptions(Arrays.asList(options));
        return createFeatureRunner(cucumberFeature, junitOption);
    }


    private FeatureRunner createFeatureRunner(CucumberFeature cucumberFeature, JUnitOptions junitOption) throws InitializationError {
        final RuntimeOptions runtimeOptions = new RuntimeOptions("-p null");
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final ClasspathResourceLoader resourceLoader = new ClasspathResourceLoader(classLoader);
        final RuntimeGlue glue = mock(RuntimeGlue.class);
        final Runtime runtime = new Runtime(resourceLoader, classLoader, asList(mock(Backend.class)), runtimeOptions, new TimeService.Stub(0l), glue);
        return new FeatureRunner(cucumberFeature, runtime, new JUnitReporter(runtime.getEventBus(), false, junitOption));
    }


    @Test
    public void shouldPopulateDescriptionsWithStableUniqueIds() throws Exception {
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
    public void shouldNotCreateStepDescriptions() throws Exception {
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

        FeatureRunner runner = createFeatureRunner(cucumberFeature, "--no-step-notifications");

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

    private static void assertDescriptionIsUnique(Description description, Set<Description> descriptions) {
        // Note: JUnit uses the the serializable parameter as the unique id when comparing Descriptions
        assertTrue(descriptions.add(description));
        for (Description each : description.getChildren()) {
            assertDescriptionIsUnique(each, descriptions);
        }
    }

    private static void assertDescriptionIsPredictable(Description description, Set<Description> descriptions) {
        assertTrue(descriptions.contains(description));
        for (Description each : description.getChildren()) {
            assertDescriptionIsPredictable(each, descriptions);
        }
    }

    private static final class DescriptionMatcher extends ArgumentMatcher<Description> {
        private String name;

        DescriptionMatcher(String name) {
            this.name = name;
        }

        @Override
        public boolean matches(Object argument) {
            return argument instanceof Description && ((Description) argument).getDisplayName().equals(name);
        }

    }

    private static final class FailureMatcher extends ArgumentMatcher<Failure> {
        private String name;

        FailureMatcher(String name) {
            this.name = name;
        }

        @Override
        public boolean matches(Object argument) {
            return argument instanceof Failure && ((Failure) argument).getDescription().getDisplayName().equals(name);
        }

    }
}

