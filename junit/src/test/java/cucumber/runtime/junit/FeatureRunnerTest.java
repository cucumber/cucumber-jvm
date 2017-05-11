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

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class FeatureRunnerTest {

    @Test
    public void should_call_formatter_for_two_scenarios_with_background() throws Throwable {
        CucumberFeature cucumberFeature = TestFeatureBuilder.feature("path/test.feature", "" +
                "Feature: feature name\n" +
                "  Background: background\n" +
                "    Given first step\n" +
                "  Scenario: scenario_1 name\n" +
                "    When second step\n" +
                "    Then third step\n" +
                "  Scenario: scenario_2 name\n" +
                "    Then second step\n");

        String formatterOutput = runFeatureWithFormatterSpy(cucumberFeature);

        assertEquals("" +
                "uri\n" +
                "feature\n" +
                "  startOfScenarioLifeCycle\n" +
                "  background\n" +
                "    step\n" +
                "    match\n" +
                "    result\n" +
                "  scenario\n" +
                "    step\n" +
                "    step\n" +
                "    match\n" +
                "    result\n" +
                "    match\n" +
                "    result\n" +
                "  endOfScenarioLifeCycle\n" +
                "  startOfScenarioLifeCycle\n" +
                "  background\n" +
                "    step\n" +
                "    match\n" +
                "    result\n" +
                "  scenario\n" +
                "    step\n" +
                "    match\n" +
                "    result\n" +
                "  endOfScenarioLifeCycle\n" +
                "eof\n", formatterOutput);
    }

    @Test
    public void should_call_formatter_for_scenario_outline_with_two_examples_table_and_background() throws Throwable {
        CucumberFeature feature = TestFeatureBuilder.feature("path/test.feature", "" +
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

        String formatterOutput = runFeatureWithFormatterSpy(feature);

        assertEquals("" +
                "uri\n" +
                "feature\n" +
                "  scenarioOutline\n" +
                "    step\n" +
                "    step\n" +
                "  examples\n" +
                "  startOfScenarioLifeCycle\n" +
                "  background\n" +
                "    step\n" +
                "    match\n" +
                "    result\n" +
                "  scenario\n" +
                "    step\n" +
                "    step\n" +
                "    match\n" +
                "    result\n" +
                "    match\n" +
                "    result\n" +
                "  endOfScenarioLifeCycle\n" +
                "  startOfScenarioLifeCycle\n" +
                "  background\n" +
                "    step\n" +
                "    match\n" +
                "    result\n" +
                "  scenario\n" +
                "    step\n" +
                "    step\n" +
                "    match\n" +
                "    result\n" +
                "    match\n" +
                "    result\n" +
                "  endOfScenarioLifeCycle\n" +
                "  examples\n" +
                "  startOfScenarioLifeCycle\n" +
                "  background\n" +
                "    step\n" +
                "    match\n" +
                "    result\n" +
                "  scenario\n" +
                "    step\n" +
                "    step\n" +
                "    match\n" +
                "    result\n" +
                "    match\n" +
                "    result\n" +
                "  endOfScenarioLifeCycle\n" +
                "eof\n", formatterOutput);
    }

    private String runFeatureWithFormatterSpy(CucumberFeature cucumberFeature) throws InitializationError {
        final RuntimeOptions runtimeOptions = new RuntimeOptions("");
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final ClasspathResourceLoader resourceLoader = new ClasspathResourceLoader(classLoader);
        final RuntimeGlue glue = mock(RuntimeGlue.class);
        final Runtime runtime = new Runtime(resourceLoader, classLoader, asList(mock(Backend.class)), runtimeOptions, new StopWatch.Stub(0l), glue);
        FormatterSpy formatterSpy = new FormatterSpy();
        FeatureRunner runner = new FeatureRunner(cucumberFeature, runtime, new JUnitReporter(formatterSpy, formatterSpy, false, new JUnitOptions(Collections.<String>emptyList())));
        runner.run(mock(RunNotifier.class));
        return formatterSpy.toString();
    }

    @Test
    public void shouldPopulateDescriptionsWithStableUniqueIds() throws Exception {
        FeatureRunner runner = createFeatureRunner();
        FeatureRunner rerunner = createFeatureRunner();

        Set<Description> descriptions = new HashSet<Description>();
        assertDescriptionIsUnique(runner.getDescription(), descriptions);
        assertDescriptionIsPredictable(rerunner.getDescription(), descriptions);
    }

    private FeatureRunner createFeatureRunner() throws IOException, InitializationError {
        CucumberFeature cucumberFeature = TestFeatureBuilder.feature("featurePath", "" +
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

        return new FeatureRunner(cucumberFeature, null, createStandardJUnitReporter());
    }

    private static void assertDescriptionIsUnique(Description description, Set<Description> descriptions) {
        // Note, JUnit uses the the serializable parameter (in this case the step)
        // as the unique id when comparing Descriptions
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

    private static JUnitReporter createStandardJUnitReporter() {
        return new JUnitReporter(null, null, false, new JUnitOptions(Collections.<String>emptyList()));
    }
}
