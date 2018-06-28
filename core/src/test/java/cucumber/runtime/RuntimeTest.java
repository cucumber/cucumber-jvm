package cucumber.runtime;

import cucumber.api.HookType;
import cucumber.api.Plugin;
import cucumber.api.Result;
import cucumber.api.Scenario;
import cucumber.api.StepDefinitionReporter;
import cucumber.runner.EventBus;
import cucumber.runner.TimeService;
import cucumber.runtime.filter.Filters;
import cucumber.runtime.filter.RerunFilters;
import cucumber.runtime.formatter.FormatterBuilder;
import cucumber.runtime.formatter.FormatterSpy;
import cucumber.runtime.formatter.PluginFactory;
import cucumber.runtime.formatter.Plugins;
import cucumber.runtime.io.ClasspathResourceLoader;
import cucumber.runtime.io.Resource;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.FeatureLoader;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleTag;
import io.cucumber.stepexpression.TypeRegistry;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
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
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollectionOf;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RuntimeTest {
    private EventBus bus;
    private ExitStatus exitStatus;

    private HookDefinition createExceptionThrowingHook() throws Throwable {
        HookDefinition hook = mock(HookDefinition.class);
        when(hook.matches(anyCollectionOf(PickleTag.class))).thenReturn(true);
        doThrow(new Exception()).when(hook).execute((Scenario) any());
        return hook;
    }

    private PickleStepDefinitionMatch createExceptionThrowingMatch(Exception exception) throws Throwable {
        PickleStepDefinitionMatch match = mock(PickleStepDefinitionMatch.class);
        doThrow(exception).when(match).runStep(anyString(), (Scenario) any());
        return match;
    }

    private Runtime createRuntime(String... runtimeArgs) {
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return createRuntime(resourceLoader, classLoader, runtimeArgs);
    }

    private Runtime createRuntime(ResourceLoader resourceLoader, ClassLoader classLoader, String... runtimeArgs) {
        RuntimeOptions runtimeOptions = new RuntimeOptions(asList(runtimeArgs));

        this.bus = new EventBus(TimeService.SYSTEM);
        Plugins plugins = new Plugins(classLoader, new PluginFactory(), bus, runtimeOptions);
        BackendSupplier backendSupplier = new BackendSupplier() {
            @Override
            public Collection<? extends Backend> get() {
                Backend backend = mock(Backend.class);
                return singletonList(backend);
            }
        };
        RuntimeGlueSupplier glueSupplier = new RuntimeGlueSupplier();
        ThreadLocalRunnerSupplier runnerSupplier = new ThreadLocalRunnerSupplier(runtimeOptions, bus, backendSupplier, glueSupplier);
        FeatureLoader featureLoader = new FeatureLoader(resourceLoader);
        RerunFilters rerunFilters = new RerunFilters(runtimeOptions, featureLoader);
        Filters filters = new Filters(runtimeOptions, rerunFilters);
        exitStatus = new ExitStatus(runtimeOptions);
        exitStatus.setEventPublisher(bus);
        FeaturePathFeatureSupplier featureSupplier = new FeaturePathFeatureSupplier(featureLoader, runtimeOptions);
        return new Runtime(plugins, bus, filters, runnerSupplier, featureSupplier);
    }

    @Test
    public void reports_step_definitions_to_plugin() throws IOException {
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        RuntimeOptions runtimeOptions = new RuntimeOptions(asList("--plugin", "cucumber.runtime.RuntimeTest$StepdefsPrinter"));
        EventBus bus = new EventBus(TimeService.SYSTEM);
        Plugins plugins = new Plugins(classLoader, new PluginFactory(), bus, runtimeOptions);
        BackendSupplier backendSupplier = new BackendSupplier() {
            @Override
            public Collection<? extends Backend> get() {
                Backend backend = mock(Backend.class);
                Collection<Backend> backends = Arrays.asList(backend);
                return backends;
            }
        };
        final StubStepDefinition stepDefinition = new StubStepDefinition("some pattern", new TypeRegistry(Locale.ENGLISH));

        GlueSupplier glueSupplier = new GlueSupplier() {
            @Override
            public Glue get() {
                Glue glue = new RuntimeGlue();
                glue.addStepDefinition(stepDefinition);
                return glue;
            }
        };
        ThreadLocalRunnerSupplier runnerSupplier = new ThreadLocalRunnerSupplier(runtimeOptions, bus, backendSupplier, glueSupplier);
        FeatureLoader featureLoader = new FeatureLoader(resourceLoader);
        RerunFilters rerunFilters = new RerunFilters(runtimeOptions, featureLoader);
        Filters filters = new Filters(runtimeOptions, rerunFilters);
        FeaturePathFeatureSupplier featureSupplier = new FeaturePathFeatureSupplier(featureLoader, runtimeOptions);
        Runtime runtime = new Runtime(plugins, bus, filters, runnerSupplier, featureSupplier);

        runtime.run();

        assertSame(stepDefinition, StepdefsPrinter.instance.stepDefinition);
    }

    @Test
    public void runs_feature_with_json_formatter() throws Exception {
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
        BackendSupplier backendSupplier = new BackendSupplier() {
            @Override
            public Collection<? extends Backend> get() {
                return singletonList(mock(Backend.class));
            }
        };
        EventBus bus = new EventBus(TimeService.SYSTEM);
        Plugins plugins = new Plugins(classLoader, new PluginFactory(), bus, runtimeOptions);
        plugins.addPlugin(jsonFormatter);
        ClasspathResourceLoader resourceLoader = new ClasspathResourceLoader(classLoader);
        FeatureLoader featureLoader = new FeatureLoader(resourceLoader);
        RerunFilters rerunFilters = new RerunFilters(runtimeOptions, featureLoader);
        Filters filters = new Filters(runtimeOptions, rerunFilters);
        RuntimeGlueSupplier glueSupplier = new RuntimeGlueSupplier();
        ThreadLocalRunnerSupplier runnerSupplier = new ThreadLocalRunnerSupplier(runtimeOptions, bus, backendSupplier, glueSupplier);
        FeatureSupplier featureSupplier = new FeatureSupplier() {
            @Override
            public List<CucumberFeature> get() {
                return singletonList(feature);
            }
        };
        Runtime runtime = new Runtime(plugins, bus, filters, runnerSupplier, featureSupplier);

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

    private String runFeatureWithFormatterSpy(CucumberFeature feature, Map<String, Result> stepsToResult) throws Throwable {
        FormatterSpy formatterSpy = new FormatterSpy();
        TestHelper.runFeatureWithFormatter(feature, stepsToResult, Collections.<SimpleEntry<String, Result>>emptyList(), 0L, formatterSpy);
        return formatterSpy.toString();
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

    private Runtime createRuntimeWithMockedGlue(PickleStepDefinitionMatch match, HookDefinition hook, HookType hookType,
                                                CucumberFeature feature, String... runtimeArgs) {
        return createRuntimeWithMockedGlue(match, false, hook, hookType, feature, runtimeArgs);
    }

    private Runtime createRuntimeWithMockedGlue(final PickleStepDefinitionMatch match, final boolean isAmbiguous,
                                                final HookDefinition hook, final HookType hookType, final CucumberFeature feature, String... runtimeArgs) {
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        ClassLoader classLoader = getClass().getClassLoader();
        List<String> args = new ArrayList<String>(asList(runtimeArgs));
        if (!args.contains("-p")) {
            args.addAll(asList("-p", "null"));
        }
        RuntimeOptions runtimeOptions = new RuntimeOptions(args);


        BackendSupplier backendSupplier = new BackendSupplier() {
            @Override
            public Collection<? extends Backend> get() {
                Backend backend = mock(Backend.class);
                return singletonList(backend);
            }
        };
        GlueSupplier glueSupplier = new GlueSupplier() {
            @Override
            public Glue get() {
                final RuntimeGlue glue = mock(RuntimeGlue.class);
                mockMatch(glue, match, isAmbiguous);
                mockHook(glue, hook, hookType);
                return glue;
            }
        };

        EventBus bus = new EventBus(TimeService.SYSTEM);
        Plugins plugins = new Plugins(classLoader, new PluginFactory(), bus, runtimeOptions);
        FeatureLoader featureLoader = new FeatureLoader(resourceLoader);
        FeatureSupplier featureSupplier = new FeatureSupplier() {
            @Override
            public List<CucumberFeature> get() {
                return singletonList(feature);
            }
        };
        ThreadLocalRunnerSupplier runnerSupplier = new ThreadLocalRunnerSupplier(runtimeOptions, bus, backendSupplier, glueSupplier);
        RerunFilters rerunFilters = new RerunFilters(runtimeOptions, featureLoader);
        Filters filters = new Filters(runtimeOptions, rerunFilters);
        return new Runtime(plugins, bus, filters, runnerSupplier, featureSupplier);
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
                when(glue.getBeforeHooks()).thenReturn(Arrays.asList(hook));
                return;
            case After:
                when(glue.getAfterHooks()).thenReturn(Arrays.asList(hook));
                return;
            case AfterStep:
                when(glue.getAfterStepHooks()).thenReturn(Arrays.asList(hook));
                return;
        }
    }

    @Test
    public void should_pass_if_no_features_are_found() throws IOException {
        ResourceLoader resourceLoader = createResourceLoaderThatFindsNoFeatures();
        Runtime runtime = createStrictRuntime(resourceLoader);

        runtime.run();

        assertEquals(0x0, exitStatus.exitStatus());
    }

    private ResourceLoader createResourceLoaderThatFindsNoFeatures() {
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        when(resourceLoader.resources(anyString(), eq(".feature"))).thenReturn(Collections.<Resource>emptyList());
        return resourceLoader;
    }

    private Runtime createStrictRuntime(ResourceLoader resourceLoader) {
        return createRuntime(resourceLoader, Thread.currentThread().getContextClassLoader(), "-g", "anything", "--strict");
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
}
