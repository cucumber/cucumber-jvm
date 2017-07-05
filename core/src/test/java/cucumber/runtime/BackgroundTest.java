package cucumber.runtime;

import cucumber.runtime.io.ClasspathResourceLoader;
import cucumber.runtime.model.CucumberFeature;
//import gherkin.formatter.PrettyFormatter;
import org.junit.Test;

import java.io.IOException;

import static cucumber.runtime.TestHelper.feature;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class BackgroundTest {
    @Test @org.junit.Ignore
    public void should_run_background() throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        RuntimeOptions runtimeOptions = new RuntimeOptions("");
        Runtime runtime = new Runtime(new ClasspathResourceLoader(classLoader), classLoader, asList(mock(Backend.class)), runtimeOptions);
        CucumberFeature feature = feature("test.feature", "" +
                "Feature:\n" +
                "  Background:\n" +
                "    Given b\n" +
                "  Scenario:\n" +
                "    When s\n");

        StringBuilder out = new StringBuilder();
        //PrettyFormatter pretty = new PrettyFormatter(out, true, true);
        //feature.run(pretty, pretty, runtime);
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
