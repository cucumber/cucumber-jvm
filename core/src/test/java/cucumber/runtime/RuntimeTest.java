package cucumber.runtime;

import cucumber.io.ClasspathResourceLoader;
import cucumber.io.ResourceLoader;
import cucumber.runtime.model.CucumberFeature;
import gherkin.I18n;
import gherkin.formatter.JSONPrettyFormatter;
import gherkin.formatter.model.Step;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static cucumber.runtime.TestHelper.feature;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class RuntimeTest {

    private static final I18n ENGLISH = new I18n("en");

    @Test
    public void runs_feature_with_json_formatter() throws Exception {
        CucumberFeature feature = feature("test.feature", "" +
                "Feature: feature name\n" +
                "  Background: background name\n" +
                "    Given b\n" +
                "  Scenario: scenario name\n" +
                "    When s\n");
        StringBuilder out = new StringBuilder();
        JSONPrettyFormatter jsonFormatter = new JSONPrettyFormatter(out);
        List<Backend> backends = asList(mock(Backend.class));
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        RuntimeOptions runtimeOptions = new RuntimeOptions();
        Runtime runtime = new Runtime(new ClasspathResourceLoader(classLoader), classLoader, backends, runtimeOptions);
        feature.run(jsonFormatter, jsonFormatter, runtime);
        jsonFormatter.done();
        String expected = "" +
                "[\n" +
                "  {\n" +
                "    \"id\": \"feature-name\",\n" +
                "    \"description\": \"\",\n" +
                "    \"name\": \"feature name\",\n" +
                "    \"keyword\": \"Feature\",\n" +
                "    \"line\": 1,\n" +
                "    \"elements\": [\n" +
                "      {\n" +
                "        \"description\": \"\",\n" +
                "        \"name\": \"background name\",\n" +
                "        \"keyword\": \"Background\",\n" +
                "        \"line\": 2,\n" +
                "        \"steps\": [\n" +
                "          {\n" +
                "            \"result\": {\n" +
                "              \"status\": \"undefined\"\n" +
                "            },\n" +
                "            \"name\": \"b\",\n" +
                "            \"keyword\": \"Given \",\n" +
                "            \"line\": 3,\n" +
                "            \"match\": {}\n" +
                "          }\n" +
                "        ],\n" +
                "        \"type\": \"background\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"id\": \"feature-name;scenario-name\",\n" +
                "        \"description\": \"\",\n" +
                "        \"name\": \"scenario name\",\n" +
                "        \"keyword\": \"Scenario\",\n" +
                "        \"line\": 4,\n" +
                "        \"steps\": [\n" +
                "          {\n" +
                "            \"result\": {\n" +
                "              \"status\": \"undefined\"\n" +
                "            },\n" +
                "            \"name\": \"s\",\n" +
                "            \"keyword\": \"When \",\n" +
                "            \"line\": 5,\n" +
                "            \"match\": {}\n" +
                "          }\n" +
                "        ],\n" +
                "        \"type\": \"scenario\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"uri\": \"test.feature\"\n" +
                "  }\n" +
                "]";
        assertEquals(expected, out.toString());
    }

    @Test
    public void strict_without_pending_steps_or_errors()
    {
        Runtime runtime = createStrictRuntime();

        assertEquals(0x0, runtime.exitStatus());
    }

    @Test
    public void non_strict_without_pending_steps_or_errors()
    {
        Runtime runtime = createNonStrictRuntime();

        assertEquals(0x0, runtime.exitStatus());
    }

    @Test
    public void non_strict_with_undefined_steps()
    {
        Runtime runtime = createNonStrictRuntime();
        runtime.undefinedStepsTracker.addUndefinedStep(new Step(null, "Given ", "A", 1, null, null), ENGLISH);
        assertEquals(0x0, runtime.exitStatus());
    }

    @Test
    public void strict_with_undefined_steps()
    {
        Runtime runtime = createStrictRuntime();
        runtime.undefinedStepsTracker.addUndefinedStep(new Step(null, "Given ", "A", 1, null, null), ENGLISH);
        assertEquals(0x1, runtime.exitStatus());
    }

    @Test
    public void strict_with_pending_steps_and_no_errors()
    {
        Runtime runtime = createStrictRuntime();
        runtime.addError(new PendingException());

        assertEquals(0x1, runtime.exitStatus());
    }

    @Test
    public void non_strict_with_pending_steps()
    {
        Runtime runtime = createNonStrictRuntime();
        runtime.addError(new PendingException());

        assertEquals(0x0, runtime.exitStatus());
    }

    @Test
    public void non_strict_with_errors()
    {
        Runtime runtime = createNonStrictRuntime();
        runtime.addError(new RuntimeException());

        assertEquals(0x1, runtime.exitStatus());
    }

    @Test
    public void strict_with_errors()
    {
        Runtime runtime = createStrictRuntime();
        runtime.addError(new RuntimeException());

        assertEquals(0x1, runtime.exitStatus());
    }

    private Runtime createStrictRuntime()
    {
        return createRuntime("-g anything", "--strict");
    }

    private Runtime createNonStrictRuntime()
    {
        return createRuntime("-g anything");
    }

    private Runtime createRuntime(String ... runtimeArgs)
    {
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        ClassLoader classLoader = mock(ClassLoader.class);
        RuntimeOptions runtimeOptions = new RuntimeOptions(runtimeArgs);
        Backend backend = mock(Backend.class);
        Collection<Backend> backends = Arrays.asList(backend);

        return new Runtime(resourceLoader, classLoader, backends, runtimeOptions);
    }
}
