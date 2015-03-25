package cucumber.runtime.formatter;

import cucumber.runtime.*;
import cucumber.runtime.Runtime;
import cucumber.runtime.io.ClasspathResourceLoader;
import cucumber.runtime.snippets.FunctionNameGenerator;
import gherkin.formatter.model.Step;
import gherkin.formatter.model.Tag;
import gherkin.deps.com.google.gson.JsonParser;
import gherkin.deps.com.google.gson.JsonElement;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static java.util.Arrays.asList;
import static java.util.Collections.sort;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JSONPrettyFormatterTest {

    @Test
    public void featureWithOutlineTest() throws Exception {
        File report = runFeaturesWithJSONPrettyFormatter(asList("cucumber/runtime/formatter/JSONPrettyFormatterTest.feature"));
        String expected = new Scanner(getClass().getResourceAsStream("JSONPrettyFormatterTest.json"), "UTF-8").useDelimiter("\\A").next();
        String actual = new Scanner(report, "UTF-8").useDelimiter("\\A").next();

        assertPrettyJsonEquals(expected, actual);
    }

    private void assertPrettyJsonEquals(final String expected, final String actual) {
        assertJsonEquals(expected, actual);

        List<String> expectedLines = sortedLinesWithWhitespace(expected);
        List<String> actualLines = sortedLinesWithWhitespace(actual);
        assertEquals(expectedLines, actualLines);
    }

    private List<String> sortedLinesWithWhitespace(final String string) {
        List<String> lines = asList(string.split(",?(?:\r\n?|\n)")); // also remove trailing ','
        sort(lines);
        return lines;
    }

    private void assertJsonEquals(final String expected, final String actual) {
        JsonParser parser = new JsonParser();
        JsonElement o1 = parser.parse(expected);
        JsonElement o2 = parser.parse(actual);
        assertEquals(o1, o2);
    }

    private File runFeaturesWithJSONPrettyFormatter(final List<String> featurePaths) throws IOException {
        HookDefinition hook = mock(HookDefinition.class);
        when(hook.matches(anyListOf(Tag.class))).thenReturn(true);
        File report = File.createTempFile("cucumber-jvm-junit", ".json");
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final ClasspathResourceLoader resourceLoader = new ClasspathResourceLoader(classLoader);

        List<String> args = new ArrayList<String>();
        args.add("--plugin");
        args.add("json:" + report.getAbsolutePath());
        args.addAll(featurePaths);

        RuntimeOptions runtimeOptions = new RuntimeOptions(args);
        Backend backend = mock(Backend.class);
        when(backend.getSnippet(any(Step.class), any(FunctionNameGenerator.class))).thenReturn("TEST SNIPPET");
        final Runtime runtime = new Runtime(resourceLoader, classLoader, asList(backend), runtimeOptions, new StopWatch.Stub(1234), null);
        runtime.getGlue().addBeforeHook(hook, HookScope.SCENARIO);
        runtime.run();
        return report;
    }

}
