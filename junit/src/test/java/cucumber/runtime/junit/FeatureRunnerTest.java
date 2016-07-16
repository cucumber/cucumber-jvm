package cucumber.runtime.junit;

import cucumber.runtime.Backend;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeGlue;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.StopWatch;
import cucumber.runtime.io.ClasspathResourceLoader;
import cucumber.runtime.model.CucumberFeature;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.mockito.ArgumentMatcher;
import org.mockito.InOrder;

import java.util.Collections;

import static java.util.Arrays.asList;
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
        order.verify(notifier).fireTestIgnored(argThat(new DescriptionMatcher("first step(scenario_1 name)")));
        order.verify(notifier).fireTestIgnored(argThat(new DescriptionMatcher("second step(scenario_1 name)")));
        order.verify(notifier).fireTestIgnored(argThat(new DescriptionMatcher("third step(scenario_1 name)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("scenario_1 name")));
        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("scenario_2 name")));
        order.verify(notifier).fireTestIgnored(argThat(new DescriptionMatcher("first step(scenario_2 name)")));
        order.verify(notifier).fireTestIgnored(argThat(new DescriptionMatcher("another second step(scenario_2 name)")));
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
        order.verify(notifier).fireTestIgnored(argThat(new DescriptionMatcher("first step(scenario outline name)")));
        order.verify(notifier).fireTestIgnored(argThat(new DescriptionMatcher("second step(scenario outline name)")));
        order.verify(notifier).fireTestIgnored(argThat(new DescriptionMatcher("third step(scenario outline name)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("scenario outline name")));
        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("scenario outline name")));
        order.verify(notifier).fireTestIgnored(argThat(new DescriptionMatcher("first step(scenario outline name)")));
        order.verify(notifier).fireTestIgnored(argThat(new DescriptionMatcher("second step(scenario outline name)")));
        order.verify(notifier).fireTestIgnored(argThat(new DescriptionMatcher("third step(scenario outline name)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("scenario outline name")));
        order.verify(notifier).fireTestStarted(argThat(new DescriptionMatcher("scenario outline name")));
        order.verify(notifier).fireTestIgnored(argThat(new DescriptionMatcher("first step(scenario outline name)")));
        order.verify(notifier).fireTestIgnored(argThat(new DescriptionMatcher("second step(scenario outline name)")));
        order.verify(notifier).fireTestIgnored(argThat(new DescriptionMatcher("third step(scenario outline name)")));
        order.verify(notifier).fireTestFinished(argThat(new DescriptionMatcher("scenario outline name")));
    }

    private RunNotifier runFeatureWithNotifier(CucumberFeature cucumberFeature) throws InitializationError {
        final RuntimeOptions runtimeOptions = new RuntimeOptions("-p null");
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final ClasspathResourceLoader resourceLoader = new ClasspathResourceLoader(classLoader);
        final RuntimeGlue glue = mock(RuntimeGlue.class);
        final Runtime runtime = new Runtime(resourceLoader, classLoader, asList(mock(Backend.class)), runtimeOptions, new StopWatch.Stub(0l), glue);
        FeatureRunner runner = new FeatureRunner(cucumberFeature, runtime, new JUnitReporter(runtime.getEventBus(), false, new JUnitOptions(Collections.<String>emptyList())));
        RunNotifier notifier = mock(RunNotifier.class);
        runner.run(notifier);
        return notifier;
    }

}

class DescriptionMatcher extends ArgumentMatcher<Description> {
    private String name;

    public DescriptionMatcher(String name) {
        this.name = name;
    }

    @Override
    public boolean matches(Object argument) {
        if (argument instanceof Description && ((Description) argument).getDisplayName().equals(name)) {
            return true;
        }
        return false;
    }

}