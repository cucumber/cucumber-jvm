package cucumber.runtime;

import cucumber.api.HookType;
import cucumber.api.Plugin;
import cucumber.api.Result;
import cucumber.api.StepDefinitionReporter;
import cucumber.api.TestCase;
import cucumber.runner.DefaultEventBus;
import cucumber.runner.EventBus;
import cucumber.runner.TimeService;
import cucumber.runtime.filter.Filters;
import cucumber.runtime.filter.RerunFilters;
import cucumber.runtime.formatter.FormatterBuilder;
import cucumber.runtime.formatter.PluginFactory;
import cucumber.runtime.formatter.Plugins;
import cucumber.runtime.model.FeatureLoader;
import io.cucumber.stepexpression.TypeRegistry;
import cucumber.api.event.TestCaseFinished;
import cucumber.api.Scenario;
import cucumber.runtime.formatter.FormatterSpy;
import cucumber.runtime.io.ClasspathResourceLoader;
import cucumber.runtime.io.Resource;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.model.CucumberFeature;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleTag;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static cucumber.runtime.TestHelper.feature;
import static cucumber.runtime.TestHelper.result;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollectionOf;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RuntimeTest {
    private final static long ANY_TIMESTAMP = 1234567890;
    private EventBus bus;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void runs_feature_with_json_formatter() {
        final CucumberFeature feature = feature("test.feature", "" +
            "Feature: feature name\n" +
            "  Background: background name\n" +
            "    Given b\n" +
            "  Scenario: scenario name\n" +
            "    When s\n");
        StringBuilder out = new StringBuilder();

        Plugin jsonFormatter = FormatterBuilder.jsonFormatter(out);
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        RuntimeOptions runtimeOptions = new RuntimeOptions("");
        Supplier<Collection<? extends Backend>> backendSupplier = new Supplier<Collection<? extends Backend>>() {
            @Override
            public Collection<? extends Backend> get() {
                return singletonList(mock(Backend.class));
            }
        };
        EventBus bus = new DefaultEventBus(TimeService.SYSTEM);
        Plugins plugins = new Plugins(classLoader, new PluginFactory(), bus, runtimeOptions);
        plugins.addPlugin(jsonFormatter);
        ClasspathResourceLoader resourceLoader = new ClasspathResourceLoader(classLoader);
        FeatureLoader featureLoader = new FeatureLoader(resourceLoader);
        RerunFilters rerunFilters = new RerunFilters(runtimeOptions, featureLoader);
        Filters filters = new Filters(runtimeOptions, rerunFilters);
        RuntimeGlueSupplier glueSupplier = new RuntimeGlueSupplier();
        RunnerSupplier runnerSupplier = new RunnerSupplier(runtimeOptions, bus, backendSupplier, glueSupplier);
        Supplier<List<CucumberFeature>> featureSupplier = new Supplier<List<CucumberFeature>>() {
            @Override
            public List<CucumberFeature> get() {
                return singletonList(feature);
            }
        };
        Runtime runtime = new Runtime(plugins, runtimeOptions, bus, filters, runnerSupplier, featureSupplier);

        runtime.run();

        String expected = "[\n" +
            "  {\n" +
            "    \"line\": 1,\n" +
            "    \"elements\": [\n" +
            "      {\n" +
            "        \"line\": 2,\n" +
            "        \"name\": \"background name\",\n" +
            "        \"description\": \"\",\n" +
            "        \"type\": \"background\",\n" +
            "        \"keyword\": \"Background\",\n" +
            "        \"steps\": [\n" +
            "          {\n" +
            "            \"result\": {\n" +
            "              \"status\": \"undefined\"\n" +
            "            },\n" +
            "            \"line\": 3,\n" +
            "            \"name\": \"b\",\n" +
            "            \"match\": {},\n" +
            "            \"keyword\": \"Given \"\n" +
            "          }\n" +
            "        ]\n" +
            "      },\n" +
            "      {\n" +
            "        \"line\": 4,\n" +
            "        \"name\": \"scenario name\",\n" +
            "        \"description\": \"\",\n" +
            "        \"id\": \"feature-name;scenario-name\",\n" +
            "        \"type\": \"scenario\",\n" +
            "        \"keyword\": \"Scenario\",\n" +
            "        \"steps\": [\n" +
            "          {\n" +
            "            \"result\": {\n" +
            "              \"status\": \"undefined\"\n" +
            "            },\n" +
            "            \"line\": 5,\n" +
            "            \"name\": \"s\",\n" +
            "            \"match\": {},\n" +
            "            \"keyword\": \"When \"\n" +
            "          }\n" +
            "        ]\n" +
            "      }\n" +
            "    ],\n" +
            "    \"name\": \"feature name\",\n" +
            "    \"description\": \"\",\n" +
            "    \"id\": \"feature-name\",\n" +
            "    \"keyword\": \"Feature\",\n" +
            "    \"uri\": \"test.feature\",\n" +
            "    \"tags\": []\n" +
            "  }\n" +
            "]";
        assertEquals(expected, out.toString());
    }

    @Test
    public void strict_with_passed_scenarios() {
        Runtime runtime = createStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Result.Type.PASSED));

        assertEquals(0x0, runtime.exitStatus());
    }

    @Test
    public void non_strict_with_passed_scenarios() {
        Runtime runtime = createNonStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Result.Type.PASSED));

        assertEquals(0x0, runtime.exitStatus());
    }

    @Test
    public void non_strict_with_undefined_scenarios() {
        Runtime runtime = createNonStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Result.Type.UNDEFINED));
        assertEquals(0x0, runtime.exitStatus());
    }

    @Test
    public void strict_with_undefined_scenarios() {
        Runtime runtime = createStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Result.Type.UNDEFINED));
        assertEquals(0x1, runtime.exitStatus());
    }

    @Test
    public void strict_with_pending_scenarios() {
        Runtime runtime = createStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Result.Type.PENDING));

        assertEquals(0x1, runtime.exitStatus());
    }

    @Test
    public void non_strict_with_pending_scenarios() {
        Runtime runtime = createNonStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Result.Type.PENDING));

        assertEquals(0x0, runtime.exitStatus());
    }

    @Test
    public void non_strict_with_skipped_scenarios() {
        Runtime runtime = createNonStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Result.Type.SKIPPED));

        assertEquals(0x0, runtime.exitStatus());
    }

    @Test
    public void strict_with_skipped_scenarios() {
        Runtime runtime = createNonStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Result.Type.SKIPPED));

        assertEquals(0x0, runtime.exitStatus());
    }

    @Test
    public void non_strict_with_failed_scenarios() {
        Runtime runtime = createNonStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Result.Type.FAILED));

        assertEquals(0x1, runtime.exitStatus());
    }

    @Test
    public void strict_with_failed_scenarios() {
        Runtime runtime = createStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Result.Type.FAILED));

        assertEquals(0x1, runtime.exitStatus());
    }

    @Test
    public void non_strict_with_ambiguous_scenarios() {
        Runtime runtime = createNonStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Result.Type.AMBIGUOUS));

        assertEquals(0x1, runtime.exitStatus());
    }

    @Test
    public void strict_with_ambiguous_scenarios() {
        Runtime runtime = createStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Result.Type.AMBIGUOUS));

        assertEquals(0x1, runtime.exitStatus());
    }

    @Test
    public void should_pass_if_no_features_are_found() {
        ResourceLoader resourceLoader = createResourceLoaderThatFindsNoFeatures();
        Runtime runtime = createStrictRuntime(resourceLoader);

        runtime.run();

        assertEquals(0x0, runtime.exitStatus());
    }

    @Test
    public void reports_step_definitions_to_plugin() {
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        RuntimeOptions runtimeOptions = new RuntimeOptions(asList("--plugin", "cucumber.runtime.RuntimeTest$StepdefsPrinter"));
        EventBus bus = new DefaultEventBus(TimeService.SYSTEM);
        Plugins plugins = new Plugins(classLoader, new PluginFactory(), bus, runtimeOptions);
        Supplier<Collection<? extends Backend>> backendSupplier = new Supplier<Collection<? extends Backend>>() {
            @Override
            public Collection<? extends Backend> get() {
                return Collections.singletonList(mock(Backend.class));
            }
        };
        final StubStepDefinition stepDefinition = new StubStepDefinition("some pattern", new TypeRegistry(Locale.ENGLISH));

        Supplier<Glue> glueSupplier = new Supplier<Glue>() {
            @Override
            public Glue get() {
                Glue glue = new RuntimeGlue();
                glue.addStepDefinition(stepDefinition);
                return glue;
            }
        };
        RunnerSupplier runnerSupplier = new RunnerSupplier(runtimeOptions, bus, backendSupplier, glueSupplier);
        FeatureLoader featureLoader = new FeatureLoader(resourceLoader);
        RerunFilters rerunFilters = new RerunFilters(runtimeOptions, featureLoader);
        Filters filters = new Filters(runtimeOptions, rerunFilters);
        FeatureSupplier featureSupplier = new FeatureSupplier(featureLoader, runtimeOptions);
        Runtime runtime = new Runtime(plugins, runtimeOptions, bus, filters, runnerSupplier, featureSupplier);

        runtime.run();

        assertSame(stepDefinition, StepdefsPrinter.instance.stepDefinition);
    }

    public static class StepdefsPrinter implements StepDefinitionReporter {
        static StepdefsPrinter instance;
        StepDefinition stepDefinition;

        public StepdefsPrinter() {
            instance = this;
        }

        @Override
        public void stepDefinition(StepDefinition stepDefinition) {
            this.stepDefinition = stepDefinition;
        }
    }

    @Test
    public void should_throw_cucumber_exception_if_no_backends_are_found() {

        expectedException.expect(CucumberException.class);
        expectedException.expectMessage("No backends were found. Please make sure you have a backend module on your CLASSPATH.");

        CucumberFeature feature = TestHelper.feature("path/test.feature",
            "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Given first step\n" +
                "    When second step\n" +
                "    Then third step\n");
        HookDefinition beforeHook = mock(HookDefinition.class);
        when(beforeHook.matches(anyCollectionOf(PickleTag.class))).thenReturn(true);

        Supplier<Collection<? extends Backend>> backendSupplier = new Supplier<Collection<? extends Backend>>() {
            @Override
            public Collection<? extends Backend> get() {
                return Collections.<Backend>emptyList();
            }
        };

        final Runtime runtime = createRuntimeWithMockedGlue(mock(PickleStepDefinitionMatch.class), false, beforeHook, HookType.Before, feature, backendSupplier);
        runtime.run();
    }

    @Test
    public void should_make_scenario_name_available_to_hooks() throws Throwable {
        CucumberFeature feature = TestHelper.feature("path/test.feature",
            "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Given first step\n" +
                "    When second step\n" +
                "    Then third step\n");
        HookDefinition beforeHook = mock(HookDefinition.class);
        when(beforeHook.matches(anyCollectionOf(PickleTag.class))).thenReturn(true);

        Runtime runtime = createRuntimeWithMockedGlue(mock(PickleStepDefinitionMatch.class), beforeHook, HookType.Before, feature);
        runtime.run();

        ArgumentCaptor<Scenario> capturedScenario = ArgumentCaptor.forClass(Scenario.class);
        verify(beforeHook).execute(capturedScenario.capture());
        assertEquals("scenario name", capturedScenario.getValue().getName());
    }

    @Test
    public void should_call_formatter_for_two_scenarios_with_background() throws Throwable {
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
            "Feature: feature name\n" +
            "  Background: background\n" +
            "    Given first step\n" +
            "  Scenario: scenario_1 name\n" +
            "    When second step\n" +
            "    Then third step\n" +
            "  Scenario: scenario_2 name\n" +
            "    Then second step\n");
        Map<String, Result> stepsToResult = new HashMap<String, Result>();
        stepsToResult.put("first step", result("passed"));
        stepsToResult.put("second step", result("passed"));
        stepsToResult.put("third step", result("passed"));

        String formatterOutput = runFeatureWithFormatterSpy(feature, stepsToResult);

        assertEquals("" +
            "TestCase started\n" +
            "  TestStep started\n" +
            "  TestStep finished\n" +
            "  TestStep started\n" +
            "  TestStep finished\n" +
            "  TestStep started\n" +
            "  TestStep finished\n" +
            "TestCase finished\n" +
            "TestCase started\n" +
            "  TestStep started\n" +
            "  TestStep finished\n" +
            "  TestStep started\n" +
            "  TestStep finished\n" +
            "TestCase finished\n" +
            "TestRun finished\n", formatterOutput);
    }

    @Test
    public void should_call_formatter_for_scenario_outline_with_two_examples_table_and_background() throws Throwable {
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
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
        Map<String, Result> stepsToResult = new HashMap<String, Result>();
        stepsToResult.put("first step", result("passed"));
        stepsToResult.put("second step", result("passed"));
        stepsToResult.put("third step", result("passed"));

        String formatterOutput = runFeatureWithFormatterSpy(feature, stepsToResult);

        assertEquals("" +
            "TestCase started\n" +
            "  TestStep started\n" +
            "  TestStep finished\n" +
            "  TestStep started\n" +
            "  TestStep finished\n" +
            "  TestStep started\n" +
            "  TestStep finished\n" +
            "TestCase finished\n" +
            "TestCase started\n" +
            "  TestStep started\n" +
            "  TestStep finished\n" +
            "  TestStep started\n" +
            "  TestStep finished\n" +
            "  TestStep started\n" +
            "  TestStep finished\n" +
            "TestCase finished\n" +
            "TestCase started\n" +
            "  TestStep started\n" +
            "  TestStep finished\n" +
            "  TestStep started\n" +
            "  TestStep finished\n" +
            "  TestStep started\n" +
            "  TestStep finished\n" +
            "TestCase finished\n" +
            "TestRun finished\n", formatterOutput);
    }

    @Test
    public void should_call_formatter_with_correct_sequence_of_events_when_running_in_parallel() throws Throwable {
        CucumberFeature feature1 = TestHelper.feature("path/test.feature", "" +
            "Feature: feature name 1\n" +
            "  Scenario: scenario_1 name\n" +
            "    Given first step\n");

        CucumberFeature feature2 = TestHelper.feature("path/test2.feature", "" +
            "Feature: feature name 2\n" +
            "  Scenario: scenario_2 name\n" +
            "    Given first step\n");

        Map<String, Result> stepsToResult = new HashMap<String, Result>();
        stepsToResult.put("first step", result("passed"));

        FormatterSpy formatterSpy = new FormatterSpy();
        final List<CucumberFeature> features = Arrays.asList(feature1, feature2);
        TestHelper.runFeaturesWithFormatter(features, stepsToResult, Collections.<SimpleEntry<String, Result>>emptyList(), 0L, formatterSpy, features.size());
        String formatterOutput = formatterSpy.toString();

        assertEquals("" +
            "TestCase started\n" +
            "  TestStep started\n" +
            "  TestStep finished\n" +
            "TestCase finished\n" +
            "TestCase started\n" +
            "  TestStep started\n" +
            "  TestStep finished\n" +
            "TestCase finished\n" +
            "TestRun finished\n", formatterOutput);
    }

    private String runFeatureWithFormatterSpy(CucumberFeature feature, Map<String, Result> stepsToResult) throws Throwable {
        FormatterSpy formatterSpy = new FormatterSpy();
        TestHelper.runFeatureWithFormatter(feature, stepsToResult, Collections.<SimpleEntry<String, Result>>emptyList(), 0L, formatterSpy);
        return formatterSpy.toString();
    }

    private ResourceLoader createResourceLoaderThatFindsNoFeatures() {
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        when(resourceLoader.resources(anyString(), eq(".feature"))).thenReturn(Collections.<Resource>emptyList());
        return resourceLoader;
    }

    private Runtime createStrictRuntime() {
        return createRuntime("-g", "anything", "--strict");
    }

    private Runtime createNonStrictRuntime() {
        return createRuntime("-g", "anything");
    }

    private Runtime createStrictRuntime(ResourceLoader resourceLoader) {
        return createRuntime(resourceLoader, Thread.currentThread().getContextClassLoader(), "-g", "anything", "--strict");
    }

    private Runtime createRuntime(String... runtimeArgs) {
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return createRuntime(resourceLoader, classLoader, runtimeArgs);
    }

    private Runtime createRuntime(ResourceLoader resourceLoader, ClassLoader classLoader, String... runtimeArgs) {
        RuntimeOptions runtimeOptions = new RuntimeOptions(asList(runtimeArgs));

        this.bus = new DefaultEventBus(TimeService.SYSTEM);
        Plugins plugins = new Plugins(classLoader, new PluginFactory(), bus, runtimeOptions);
        Supplier<Collection<? extends Backend>> backendSupplier = new Supplier<Collection<? extends Backend>>() {
            @Override
            public Collection<? extends Backend> get() {
                Backend backend = mock(Backend.class);
                return singletonList(backend);
            }
        };
        RuntimeGlueSupplier glueSupplier = new RuntimeGlueSupplier();
        RunnerSupplier runnerSupplier = new RunnerSupplier(runtimeOptions, bus, backendSupplier, glueSupplier);
        FeatureLoader featureLoader = new FeatureLoader(resourceLoader);
        RerunFilters rerunFilters = new RerunFilters(runtimeOptions, featureLoader);
        Filters filters = new Filters(runtimeOptions, rerunFilters);
        FeatureSupplier featureSupplier = new FeatureSupplier(featureLoader, runtimeOptions);
        return new Runtime(plugins, runtimeOptions, bus, filters, runnerSupplier, featureSupplier);
    }

    private Runtime createRuntimeWithMockedGlue(PickleStepDefinitionMatch match, HookDefinition hook, HookType hookType,
                                                CucumberFeature feature, String... runtimeArgs) {
        return createRuntimeWithMockedGlue(match, false, hook, hookType, feature, null, runtimeArgs);
    }

    private Runtime createRuntimeWithMockedGlue(final PickleStepDefinitionMatch match, final boolean isAmbiguous,
                                                final HookDefinition hook, final HookType hookType,
                                                final CucumberFeature feature,
                                                final Supplier<Collection<? extends Backend>> backendSupplier,
                                                final String... runtimeArgs) {
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        ClassLoader classLoader = getClass().getClassLoader();
        List<String> args = new ArrayList<String>(asList(runtimeArgs));
        if (!args.contains("-p")) {
            args.addAll(asList("-p", "null"));
        }
        RuntimeOptions runtimeOptions = new RuntimeOptions(args);

        Supplier<Collection<? extends Backend>> backends = backendSupplier != null
            ? backendSupplier
            : new Supplier<Collection<? extends Backend>>() {
                @Override
                public Collection<? extends Backend> get() {
                    return Collections.singletonList(mock(Backend.class));
                }
            };
        Supplier<Glue> glueSupplier = new Supplier<Glue>() {
            @Override
            public Glue get() {
                final RuntimeGlue glue = mock(RuntimeGlue.class);
                mockMatch(glue, match, isAmbiguous);
                mockHook(glue, hook, hookType);
                return glue;
            }
        };

        EventBus bus = new DefaultEventBus(TimeService.SYSTEM);
        Plugins plugins = new Plugins(classLoader, new PluginFactory(), bus, runtimeOptions);
        FeatureLoader featureLoader = new FeatureLoader(resourceLoader);
        Supplier<List<CucumberFeature>> featureSupplier = new Supplier<List<CucumberFeature>>() {
            @Override
            public List<CucumberFeature> get() {
                return singletonList(feature);
            }
        };
        RunnerSupplier runnerSupplier = new RunnerSupplier(runtimeOptions, bus, backends, glueSupplier);
        RerunFilters rerunFilters = new RerunFilters(runtimeOptions, featureLoader);
        Filters filters = new Filters(runtimeOptions, rerunFilters);
        return new Runtime(plugins, runtimeOptions, bus, filters, runnerSupplier, featureSupplier);
    }

    private void mockMatch(RuntimeGlue glue, PickleStepDefinitionMatch match, boolean isAmbiguous) {
        if (isAmbiguous) {
            Exception exception = new AmbiguousStepDefinitionsException(mock(PickleStep.class), Arrays.asList(match, match));
            doThrow(exception).when(glue).stepDefinitionMatch(anyString(), (PickleStep) any());
        } else {
            when(glue.stepDefinitionMatch(anyString(), (PickleStep) any())).thenReturn(match);
        }
    }

    private void mockHook(RuntimeGlue glue, HookDefinition hook, HookType hookType) {
        switch (hookType) {
            case Before:
                when(glue.getBeforeHooks()).thenReturn(Collections.singletonList(hook));
                return;
            case After:
                when(glue.getAfterHooks()).thenReturn(Collections.singletonList(hook));
                return;
            case AfterStep:
                when(glue.getAfterStepHooks()).thenReturn(Collections.singletonList(hook));
                return;
        }
    }

    private TestCaseFinished testCaseFinishedWithStatus(Result.Type resultStatus) {
        return new TestCaseFinished(ANY_TIMESTAMP, mock(TestCase.class), new Result(resultStatus, null, null));
    }
}
