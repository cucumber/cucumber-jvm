package cucumber.runtime.formatter;

import cucumber.runtime.Backend;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.io.ClasspathResourceLoader;
import gherkin.formatter.model.Step;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JSONPrettyFormatterTest {

    @Test
    public void featureWithOutlineTest() throws Exception {
        File report = runFeaturesWithJSONPrettyFormatter(asList("cucumber/runtime/formatter/JSONPrettyFormatterTest.feature"));
        String expected = new Scanner(getClass().getResourceAsStream("JSONPrettyFormatterTest.json"), "UTF-8").useDelimiter("\\A").next();
        expected = expected.replace("cucumber/runtime/formatter/JSONPrettyFormatterTest.feature", "cucumber" + File.separator + "runtime"
                + File.separator + "formatter" + File.separator + "JSONPrettyFormatterTest.feature");
        String actual = new Scanner(report, "UTF-8").useDelimiter("\\A").next();
        assertEquals(expected, actual);
    }

    private File runFeaturesWithJSONPrettyFormatter(final List<String> featurePaths) throws IOException {
        File report = File.createTempFile("cucumber-jvm-junit", ".json");
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final ClasspathResourceLoader resourceLoader = new ClasspathResourceLoader(classLoader);

        List<String> args = new ArrayList<String>();
        args.add("--format");
        args.add("json-pretty:" + report.getAbsolutePath());
        args.addAll(featurePaths);

        RuntimeOptions runtimeOptions = new RuntimeOptions(new Properties(), args.toArray(new String[args.size()]));
        Backend backend = mock(Backend.class);
        when(backend.getSnippet(any(Step.class))).thenReturn("TEST SNIPPET");
        final cucumber.runtime.Runtime runtime = new Runtime(resourceLoader, classLoader, asList(backend), runtimeOptions);
        runtime.run();
        return report;
    }

}
