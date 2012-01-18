package cucumber.runtime;

import cucumber.io.ClasspathResourceLoader;
import cucumber.runtime.model.CucumberFeature;
import gherkin.formatter.PrettyFormatter;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

import static cucumber.runtime.TestHelper.feature;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class BackgroundTest {
    @Test
    public void should_run_background() throws IOException {
        Runtime runtime = new Runtime(new ArrayList<String>(), new ClasspathResourceLoader(), asList(mock(Backend.class)), false);
        CucumberFeature feature = feature("test.feature", "" +
                "Feature:\n" +
                "  Background:\n" +
                "    Given b\n" +
                "  Scenario:\n" +
                "    When s\n");

        StringBuilder out = new StringBuilder();
        PrettyFormatter pretty = new PrettyFormatter(out, true, true);
        runtime.run(feature, pretty, pretty);
        String expectedOutput = "" +
                "Feature: \n" +
                "\n" +
                "  Background:  # test.feature:2\n" +
                "    Given b\n" +
                "\n" +
                "  Scenario:  # test.feature:4\n" +
                "    When s\n";
        assertEquals(expectedOutput, out.toString());
    }

    // TODO: Add some negative tests to verify how it behaves with failure

}
