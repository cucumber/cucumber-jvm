package cucumber.runtime;

import cucumber.runtime.model.CucumberFeature;
import gherkin.formatter.PrettyFormatter;
import gherkin.formatter.model.Step;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static cucumber.runtime.TestHelper.feature;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class BackgroundTest {
    @Test
    public void should_run_background() {
        Backend backend = new TestBackend();
        Runtime runtime = new Runtime(new ArrayList<String>(), asList(backend));
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

    private class TestBackend implements Backend {
        @Override
        public void buildWorld(List<String> codePaths, World world) {
        }

        @Override
        public void disposeWorld() {
        }

        @Override
        public String getSnippet(Step step) {
            return "SNIP";
        }
    }
}
