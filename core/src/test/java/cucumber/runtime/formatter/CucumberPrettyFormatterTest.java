package cucumber.runtime.formatter;

import static cucumber.runtime.TestHelper.feature;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import gherkin.I18n;
import gherkin.formatter.model.Step;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;

import cucumber.runtime.Backend;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeGlue;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.StepDefinitionMatch;
import cucumber.runtime.io.ClasspathResourceLoader;
import cucumber.runtime.model.CucumberFeature;

public class CucumberPrettyFormatterTest {

    @Test
    public void should_align_the_indentation_of_location_strings() throws IOException {
        CucumberFeature feature = feature("path/test.feature",
                "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Given first step\n" +
                "    When second step\n" +
                "    Then third step\n");
        Map<String, String> stepsToLocation = new HashMap<String, String>();
        stepsToLocation.put("first step", "path/step_definitions.java:3");
        stepsToLocation.put("second step", "path/step_definitions.java:7");
        stepsToLocation.put("third step", "path/step_definitions.java:11");

        String formatterOutput = runFeatureWithPrettyFormatter(feature, stepsToLocation);

        assertThat(formatterOutput, containsString(
                "  Scenario: scenario name # path/test.feature:2\n" +
                "    Given first step      # path/step_definitions.java:3\n" +
                "    When second step      # path/step_definitions.java:7\n" +
                "    Then third step       # path/step_definitions.java:11\n"));
    }

    private String runFeatureWithPrettyFormatter(final CucumberFeature feature, final Map<String, String> stepsToLocation) throws IOException {
        final RuntimeOptions runtimeOptions = new RuntimeOptions(new Properties());
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final ClasspathResourceLoader resourceLoader = new ClasspathResourceLoader(classLoader);
        final RuntimeGlue glue = createMockedRuntimeGlueThatMatchesTheSteps(stepsToLocation);
        final Runtime runtime = new Runtime(resourceLoader, classLoader, asList(mock(Backend.class)), runtimeOptions, glue);
        final StringBuilder out = new StringBuilder();
        final CucumberPrettyFormatter prettyFormatter = new CucumberPrettyFormatter(out);
        prettyFormatter.setMonochrome(true);

        feature.run(prettyFormatter, prettyFormatter, runtime);

        return out.toString();
    }

    private RuntimeGlue createMockedRuntimeGlueThatMatchesTheSteps(Map<String, String> stepsToLocation) {
        RuntimeGlue glue = mock(RuntimeGlue.class);
        for (String stepName : stepsToLocation.keySet()) {
            StepDefinitionMatch matchStep = mock(StepDefinitionMatch.class);
            when(matchStep.getLocation()).thenReturn(stepsToLocation.get(stepName));
            when(glue.stepDefinitionMatch(anyString(), stepWithName(stepName), (I18n)any())).thenReturn(matchStep);
        }
        return glue;
    }

    private Step stepWithName(String name) {
        return argThat(new StepMatcher(name));
    }
}
