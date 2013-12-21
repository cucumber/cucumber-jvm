package cucumber.runtime.formatter;

import cucumber.runtime.TestHelper;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.InvalidCucumberFeature;

import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static cucumber.runtime.TestHelper.feature;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.startsWith;;
import static org.junit.Assert.*;
import static org.junit.Assert.assertThat;

public class CucumberPrettyFormatterTest {

    @Test
    public void should_align_the_indentation_of_location_strings() throws Throwable {
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
    
    @Test
    public void should_handle_invalid_feature() throws Throwable {
        final Exception error = new Exception("Error");
        final CucumberFeature feature = new InvalidCucumberFeature("Reason", "path/File", error);
        final String output = runFeatureWithPrettyFormatter(feature, new HashMap<String, String>());
        final String expected = "Feature: File\n" + 
                "  path/File\n" +
                "\n" +
                "  Error: Reason # path/File:0\n" +
                "    CauseError\n" +
                "      java.lang.Exception: Error\n";
        assertThat(output, startsWith(expected));
    }

    private String runFeatureWithPrettyFormatter(final CucumberFeature feature, final Map<String, String> stepsToLocation) throws Throwable {
        final StringBuilder out = new StringBuilder();
        final CucumberPrettyFormatter prettyFormatter = new CucumberPrettyFormatter(out);
        prettyFormatter.setMonochrome(true);
        TestHelper.runFeatureWithFormatter(feature, stepsToLocation, prettyFormatter, prettyFormatter);
        return out.toString();
    }

}
