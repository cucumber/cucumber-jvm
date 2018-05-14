package cucumber.runtime;

import cucumber.api.HookType;
import cucumber.api.PendingException;
import cucumber.api.Result;
import cucumber.api.StepDefinitionReporter;
import cucumber.api.event.TestCaseFinished;
import cucumber.api.Scenario;
import cucumber.api.TestCase;
import cucumber.api.event.EventHandler;
import cucumber.api.event.TestGroupRunFinished;
import cucumber.api.event.TestGroupRunStarted;
import cucumber.runner.EventBus;
import cucumber.runtime.formatter.FormatterSpy;
import cucumber.runtime.io.ClasspathResourceLoader;
import cucumber.runtime.io.Resource;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.model.CucumberFeature;
import gherkin.events.PickleEvent;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleTag;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cucumber.runtime.TestHelper.feature;
import static cucumber.runtime.TestHelper.result;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class RuntimeTest {
    private final static String ENGLISH = "en";
    private final static long ANY_TIMESTAMP = 1234567890;
    
    private Backend backend;
    private RuntimeGlue templateGlue;
    private RuntimeGlue localGlue;
    private RuntimeOptions runtimeOptions;

    private final List<TestGroupRunStarted> actualGroupStartEvents = new ArrayList<TestGroupRunStarted>();
    private final List<TestGroupRunFinished> actualGroupEndEvents = new ArrayList<TestGroupRunFinished>();
    
    private final EventHandler<TestGroupRunStarted> handleTestGroupRunStarted = new EventHandler<TestGroupRunStarted>() {
        @Override
        public void receive(final TestGroupRunStarted event) {
            actualGroupStartEvents.add(event);
        }
    };
    
    private final EventHandler<TestGroupRunFinished> handleTestGroupRunFinished = new EventHandler<TestGroupRunFinished>() {
        @Override
        public void receive(final TestGroupRunFinished event) {
            actualGroupEndEvents.add(event);
        }
    };

    @Ignore
    @Test
    public void runs_feature_with_json_formatter() throws Exception {
        CucumberFeature feature = feature("test.feature", "" +
                "Feature: feature name\n" +
                "  Background: background name\n" +
                "    Given b\n" +
                "  Scenario: scenario name\n" +
                "    When s\n");
        StringBuilder out = new StringBuilder();

//        JSONFormatter jsonFormatter = new JSONFormatter(out);
        List<Backend> backends = asList(mock(Backend.class));
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        RuntimeOptions runtimeOptions = new RuntimeOptions("");
        Runtime runtime = new Runtime(new ClasspathResourceLoader(classLoader), classLoader, backends, runtimeOptions);
//        feature.run(jsonFormatter, jsonFormatter, runtime);
//        jsonFormatter.done();
//        String expected = "" +
//                "[\n" +
//                "  {\n" +
//                "    \"id\": \"feature-name\",\n" +
//                "    \"description\": \"\",\n" +
//                "    \"name\": \"feature name\",\n" +
//                "    \"keyword\": \"Feature\",\n" +
//                "    \"line\": 1,\n" +
//                "    \"elements\": [\n" +
//                "      {\n" +
//                "        \"description\": \"\",\n" +
//                "        \"name\": \"background name\",\n" +
//                "        \"keyword\": \"Background\",\n" +
//                "        \"line\": 2,\n" +
//                "        \"steps\": [\n" +
//                "          {\n" +
//                "            \"result\": {\n" +
//                "              \"status\": \"undefined\"\n" +
//                "            },\n" +
//                "            \"name\": \"b\",\n" +
//                "            \"keyword\": \"Given \",\n" +
//                "            \"line\": 3,\n" +
//                "            \"match\": {}\n" +
//                "          }\n" +
//                "        ],\n" +
//                "        \"type\": \"background\"\n" +
//                "      },\n" +
//                "      {\n" +
//                "        \"id\": \"feature-name;scenario-name\",\n" +
//                "        \"description\": \"\",\n" +
//                "        \"name\": \"scenario name\",\n" +
//                "        \"keyword\": \"Scenario\",\n" +
//                "        \"line\": 4,\n" +
//                "        \"steps\": [\n" +
//                "          {\n" +
//                "            \"result\": {\n" +
//                "              \"status\": \"undefined\"\n" +
//                "            },\n" +
//                "            \"name\": \"s\",\n" +
//                "            \"keyword\": \"When \",\n" +
//                "            \"line\": 5,\n" +
//                "            \"match\": {}\n" +
//                "          }\n" +
//                "        ],\n" +
//                "        \"type\": \"scenario\"\n" +
//                "      }\n" +
//                "    ],\n" +
//                "    \"uri\": \"test.feature\"\n" +
//                "  }\n" +
//                "]";
//        assertEquals(expected, out.toString());
    }

    @Test
    public void strict_with_passed_scenarios() {
        Runtime runtime = createStrictRuntime();
        runtime.getEventBus().send(testCaseFinishedWithStatus(Result.Type.PASSED));

        assertEquals(0x0, runtime.exitStatus());
    }

    @Test
    public void non_strict_with_passed_scenarios() {
        Runtime runtime = createNonStrictRuntime();
        runtime.getEventBus().send(testCaseFinishedWithStatus(Result.Type.PASSED));

        assertEquals(0x0, runtime.exitStatus());
    }

    @Test
    public void non_strict_with_undefined_scenarios() {
        Runtime runtime = createNonStrictRuntime();
        runtime.getEventBus().send(testCaseFinishedWithStatus(Result.Type.UNDEFINED));
        assertEquals(0x0, runtime.exitStatus());
    }

    @Test
    public void strict_with_undefined_scenarios() {
        Runtime runtime = createStrictRuntime();
        runtime.getEventBus().send(testCaseFinishedWithStatus(Result.Type.UNDEFINED));
        assertEquals(0x1, runtime.exitStatus());
    }

    @Test
    public void strict_with_pending_scenarios() {
        Runtime runtime = createStrictRuntime();
        runtime.getEventBus().send(testCaseFinishedWithStatus(Result.Type.PENDING));

        assertEquals(0x1, runtime.exitStatus());
    }

    @Test
    public void non_strict_with_pending_scenarios() {
        Runtime runtime = createNonStrictRuntime();
        runtime.getEventBus().send(testCaseFinishedWithStatus(Result.Type.PENDING));

        assertEquals(0x0, runtime.exitStatus());
    }

    @Test
    public void non_strict_with_skipped_scenarios() {
        Runtime runtime = createNonStrictRuntime();
        runtime.getEventBus().send(testCaseFinishedWithStatus(Result.Type.SKIPPED));

        assertEquals(0x0, runtime.exitStatus());
    }

    @Test
    public void strict_with_skipped_scenarios() {
        Runtime runtime = createNonStrictRuntime();
        runtime.getEventBus().send(testCaseFinishedWithStatus(Result.Type.SKIPPED));

        assertEquals(0x0, runtime.exitStatus());
    }

    @Test
    public void non_strict_with_failed_scenarios() {
        Runtime runtime = createNonStrictRuntime();
        runtime.getEventBus().send(testCaseFinishedWithStatus(Result.Type.FAILED));

        assertEquals(0x1, runtime.exitStatus());
    }

    @Test
    public void strict_with_failed_scenarios() {
        Runtime runtime = createStrictRuntime();
        runtime.getEventBus().send(testCaseFinishedWithStatus(Result.Type.FAILED));

        assertEquals(0x1, runtime.exitStatus());
    }

    @Test
    public void non_strict_with_ambiguous_scenarios() {
        Runtime runtime = createNonStrictRuntime();
        runtime.getEventBus().send(testCaseFinishedWithStatus(Result.Type.AMBIGUOUS));

        assertEquals(0x1, runtime.exitStatus());
    }

    @Test
    public void strict_with_ambiguous_scenarios() {
        Runtime runtime = createStrictRuntime();
        runtime.getEventBus().send(testCaseFinishedWithStatus(Result.Type.AMBIGUOUS));

        assertEquals(0x1, runtime.exitStatus());
    }

    @Test
    public void should_pass_if_no_features_are_found() throws IOException {
        ResourceLoader resourceLoader = createResourceLoaderThatFindsNoFeatures();
        Runtime runtime = createStrictRuntime(resourceLoader);

        runtime.run();

        assertEquals(0x0, runtime.exitStatus());
    }

    @Test
    public void reports_step_definitions_to_plugin() throws IOException, NoSuchMethodException {
        Runtime runtime = createRuntime("--plugin", "cucumber.runtime.RuntimeTest$StepdefsPrinter");

        StubStepDefinition stepDefinition = new StubStepDefinition(this, getClass().getMethod("reports_step_definitions_to_plugin"), "some pattern");
        runtime.getGlue().addStepDefinition(stepDefinition);
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
    public void should_throw_cucumer_exception_if_no_backends_are_found() throws Exception {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            new Runtime(new ClasspathResourceLoader(classLoader), classLoader, Collections.<Backend>emptyList(),
                    new RuntimeOptions(""));
            fail("A CucumberException should have been thrown");
        } catch (CucumberException e) {
            assertEquals("No backends were found. Please make sure you have a backend module on your CLASSPATH.", e.getMessage());
        }
    }

    @Test
    public void should_add_passed_result_to_the_summary_counter() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PickleStepDefinitionMatch match = mock(PickleStepDefinitionMatch.class);

        Runtime runtime = createRuntimeWithMockedGlue(match, "--monochrome");
        runScenario(runtime, stepCount(1));
        runtime.printStats(new PrintStream(baos));

        assertThat(baos.toString(), startsWith(String.format(
                "1 Scenarios (1 passed)%n" +
                        "1 Steps (1 passed)%n")));
    }

    @Test
    public void should_add_pending_result_to_the_summary_counter() throws Throwable {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PickleStepDefinitionMatch match = createExceptionThrowingMatch(new PendingException());

        Runtime runtime = createRuntimeWithMockedGlue(match, "--monochrome");
        runScenario(runtime, stepCount(1));
        runtime.printStats(new PrintStream(baos));

        assertThat(baos.toString(), containsString(String.format("" +
                "1 Scenarios (1 pending)%n" +
                "1 Steps (1 pending)%n")));
    }

    @Test
    public void should_add_failed_result_to_the_summary_counter() throws Throwable {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PickleStepDefinitionMatch match = createExceptionThrowingMatch(new Exception());

        Runtime runtime = createRuntimeWithMockedGlue(match, "--monochrome");
        runScenario(runtime, stepCount(1));
        runtime.printStats(new PrintStream(baos));

        assertThat(baos.toString(), containsString(String.format("" +
                "1 Scenarios (1 failed)%n" +
                "1 Steps (1 failed)%n")));
    }

    @Test
    public void should_add_ambiguous_match_as_failed_result_to_the_summary_counter() throws Throwable {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Runtime runtime = createRuntimeWithMockedGlueWithAmbiguousMatch("--monochrome");
        runScenario(runtime, stepCount(1));
        runtime.printStats(new PrintStream(baos));

        assertThat(baos.toString(), containsString(String.format(""+
                "1 Scenarios (1 ambiguous)%n" +
                "1 Steps (1 ambiguous)%n")));
    }

    @Test
    public void should_add_skipped_result_to_the_summary_counter() throws Throwable {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PickleStepDefinitionMatch match = createExceptionThrowingMatch(new Exception());

        Runtime runtime = createRuntimeWithMockedGlue(match, "--monochrome");
        runScenario(runtime, stepCount(2));
        runtime.printStats(new PrintStream(baos));

        assertThat(baos.toString(), containsString(String.format("" +
                "1 Scenarios (1 failed)%n" +
                "2 Steps (1 failed, 1 skipped)%n")));
    }

    @Test
    public void should_add_undefined_result_to_the_summary_counter() throws Throwable {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Runtime runtime = createRuntimeWithMockedGlue(null, "--monochrome");
        runScenario(runtime, stepCount(1));
        runtime.printStats(new PrintStream(baos));

        assertThat(baos.toString(), containsString(String.format("" +
                "1 Scenarios (1 undefined)%n" +
                "1 Steps (1 undefined)%n")));
    }

    @Test
    public void should_fail_the_scenario_if_before_fails() throws Throwable {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PickleStepDefinitionMatch match = mock(PickleStepDefinitionMatch.class);
        HookDefinition hook = createExceptionThrowingHook();

        Runtime runtime = createRuntimeWithMockedGlue(match, hook, HookType.Before, "--monochrome");
        runScenario(runtime, stepCount(1));
        runtime.printStats(new PrintStream(baos));

        assertThat(baos.toString(), containsString(String.format("" +
                "1 Scenarios (1 failed)%n" +
                "1 Steps (1 skipped)%n")));
    }

    @Test
    public void should_fail_the_scenario_if_after_fails() throws Throwable {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PickleStepDefinitionMatch match = mock(PickleStepDefinitionMatch.class);
        HookDefinition hook = createExceptionThrowingHook();

        Runtime runtime = createRuntimeWithMockedGlue(match, hook, HookType.After, "--monochrome");
        runScenario(runtime, stepCount(1));
        runtime.printStats(new PrintStream(baos));

        assertThat(baos.toString(), containsString(String.format("" +
                "1 Scenarios (1 failed)%n" +
                "1 Steps (1 passed)%n")));
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

        Runtime runtime = createRuntimeWithMockedGlue(mock(PickleStepDefinitionMatch.class), beforeHook, HookType.Before);
        runtime.runFeature(feature);

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
    public void should_ensure_glue_template_is_globally_initialized_and_then_cloned_per_thread_when_running_in_parallel() throws IOException {
        final List<String> features = asList(
            "cucumber/runtime/ParallelTests1.feature", 
            "cucumber/runtime/ParallelTests2.feature", 
            "cucumber/runtime/ParallelTests3.feature", 
            "cucumber/runtime/ParallelTests4.feature");

        final int threads = features.size();
        final Runtime runtime = createRuntimeWithMockedGlue(mock(PickleStepDefinitionMatch.class), features,"--threads", String.valueOf(threads));
        final EventBus bus = runtime.getEventBus();
        bus.registerHandlerFor(TestGroupRunStarted.class, handleTestGroupRunStarted);
        bus.registerHandlerFor(TestGroupRunFinished.class, handleTestGroupRunFinished);
        
        runtime.run();
        
        // Cannot verify these methods in order
        // e.g as each thread could've complete before the other and would result in it calling disposeWorld
        // before the other thread has finished with stepDefinitionMatch
        verify(backend).loadGlue(templateGlue, runtimeOptions.getGlue());
        verify(backend).setUnreportedStepExecutor(isA(UnreportedStepExecutor.class));
        verify(templateGlue).reportStepDefinitions(isA(StepDefinitionReporter.class));
        verify(templateGlue, times(threads)).clone();
        verify(backend, times(threads)).buildWorld(localGlue);
        verify(backend, times(threads)).disposeWorld(localGlue);
        final int stepsPerFeature = 6;
        verify(localGlue, times(threads)).getBeforeHooks();
        verify(localGlue, times(threads * stepsPerFeature)).getBeforeStepHooks();
        verify(localGlue, times(threads)).getAfterHooks();
        verify(localGlue, times(threads * stepsPerFeature)).getAfterStepHooks();
        for(final String feature : features) {
            verify(localGlue, times(stepsPerFeature)).stepDefinitionMatch(eq(feature), isA(PickleStep.class));
        }
        verifyNoMoreInteractions(templateGlue, backend, localGlue);        
    }

    @Test
    public void should_not_raise_synchronized_group_event_when_no_synchronized_features_exist() throws IOException {
        final List<String> features = asList(
            "cucumber/runtime/ParallelTests1.feature",
            "cucumber/runtime/ParallelTests2.feature",
            "cucumber/runtime/ParallelTests3.feature",
            "cucumber/runtime/ParallelTests4.feature");

        final int threads = features.size();
        final Runtime runtime = createRuntimeWithMockedGlue(mock(PickleStepDefinitionMatch.class), features,"--threads", String.valueOf(threads));
        final EventBus bus = runtime.getEventBus();
        bus.registerHandlerFor(TestGroupRunStarted.class, handleTestGroupRunStarted);
        bus.registerHandlerFor(TestGroupRunFinished.class, handleTestGroupRunFinished);

        runtime.run();

        assertEquals("Group Start event count was not as expected", 1, actualGroupStartEvents.size());
        TestGroupRunStarted started = actualGroupStartEvents.get(0);
        assertEquals("Tag not as expected", Runtime.NOT_SYNCHRONIZED_TAG, started.getType());
        assertEquals("Non Sync run thread count not as expected", threads, started.getThreadCount());
        assertEquals("Non Sync feature count not as expected", features.size(), started.getFeatureCount());

        assertEquals("Group Finished event count was not as expected", 1, actualGroupEndEvents.size());
        final TestGroupRunFinished finished = actualGroupEndEvents.get(0);
        assertEquals("Tag not as expected", Runtime.NOT_SYNCHRONIZED_TAG, finished.getType());
    }
    
    @Test
    public void should_run_synchronized_tests_together_prior_to_tests_which_are_not_synchronized() throws IOException {
        final List<String> syncFeatures = asList(
            "cucumber/runtime/SynchronizedGroup0Test1.feature",
            "cucumber/runtime/SynchronizedGroup1Test1.feature",
            "cucumber/runtime/SynchronizedGroup1Test2.feature",
            "cucumber/runtime/SynchronizedGroup1Test3.feature",
            "cucumber/runtime/SynchronizedGroup2Test1.feature",
            "cucumber/runtime/SynchronizedGroup3Test1.feature",
            "cucumber/runtime/SynchronizedGroup4Test1.feature");
        
        final List<String> parallelFeatures = asList(
            "cucumber/runtime/ParallelTests1.feature",
            "cucumber/runtime/ParallelTests2.feature",
            "cucumber/runtime/ParallelTests3.feature",
            "cucumber/runtime/ParallelTests4.feature");
        
        final List<String> allFeatures = new ArrayList<String>();
        allFeatures.addAll(parallelFeatures);
        allFeatures.addAll(syncFeatures);

        final int maxThreads = 2; //allFeatures.size();
        final Runtime runtime = createRuntimeWithMockedGlue(mock(PickleStepDefinitionMatch.class), allFeatures,"--threads", String.valueOf(maxThreads));

        
        final EventBus bus = runtime.getEventBus();
        bus.registerHandlerFor(TestGroupRunStarted.class, handleTestGroupRunStarted);
        bus.registerHandlerFor(TestGroupRunFinished.class, handleTestGroupRunFinished);

        runtime.run();
        
        assertEquals("Group Start event count was not as expected", 2, actualGroupStartEvents.size());
        TestGroupRunStarted started = actualGroupStartEvents.get(0);
        assertEquals("Tag not as expected", Runtime.SYNCHRONIZED_TAG, started.getType());
        assertEquals("Sync run thread count not as expected", maxThreads, started.getThreadCount());
        assertEquals("Sync feature count not as expected", syncFeatures.size(), started.getFeatureCount());

        started = actualGroupStartEvents.get(1);
        assertEquals("Tag not as expected", Runtime.NOT_SYNCHRONIZED_TAG, started.getType());
        assertEquals("Non Sync run thread count not as expected", maxThreads, started.getThreadCount());
        assertEquals("Non Sync feature count not as expected", parallelFeatures.size(), started.getFeatureCount());
        
        assertEquals("Group Finished event count was not as expected", 2, actualGroupEndEvents.size());
        TestGroupRunFinished finished = actualGroupEndEvents.get(0);
        assertEquals("Tag not as expected", Runtime.SYNCHRONIZED_TAG, finished.getType());
        finished = actualGroupEndEvents.get(1);
        assertEquals("Tag not as expected", Runtime.NOT_SYNCHRONIZED_TAG, finished.getType());
    }
    
    
    private String runFeatureWithFormatterSpy(CucumberFeature feature, Map<String, Result> stepsToResult) throws Throwable {
        FormatterSpy formatterSpy = new FormatterSpy();
        TestHelper.runFeatureWithFormatter(feature, stepsToResult, Collections.<SimpleEntry<String, Result>>emptyList(), 0L, formatterSpy);
        return formatterSpy.toString();
    }

    private PickleStepDefinitionMatch createExceptionThrowingMatch(Exception exception) throws Throwable {
        PickleStepDefinitionMatch match = mock(PickleStepDefinitionMatch.class);
        doThrow(exception).when(match).runStep(anyString(), (Scenario) any());
        return match;
    }

    private HookDefinition createExceptionThrowingHook() throws Throwable {
        HookDefinition hook = mock(HookDefinition.class);
        when(hook.matches(anyCollectionOf(PickleTag.class))).thenReturn(true);
        doThrow(new Exception()).when(hook).execute((Scenario) any());
        return hook;
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
        Backend backend = mock(Backend.class);
        Collection<Backend> backends = Arrays.asList(backend);

        return new Runtime(resourceLoader, classLoader, backends, runtimeOptions);
    }

    private Runtime createRuntimeWithMockedGlue(PickleStepDefinitionMatch match, List<String> featurePaths, String... runtimeArgs) {
        return createRuntimeWithMockedGlue(match, false, mock(HookDefinition.class), HookType.After, featurePaths, runtimeArgs);
    }
    
    private Runtime createRuntimeWithMockedGlue(PickleStepDefinitionMatch match, String... runtimeArgs) {
        return createRuntimeWithMockedGlue(match, false, mock(HookDefinition.class), HookType.After, Collections.<String>emptyList(), runtimeArgs);
    }

    private Runtime createRuntimeWithMockedGlue(PickleStepDefinitionMatch match, HookDefinition hook, HookType hookType, String... runtimeArgs) {
        return createRuntimeWithMockedGlue(match, false, hook, hookType, Collections.<String>emptyList(), runtimeArgs);
    }

    private Runtime createRuntimeWithMockedGlueWithAmbiguousMatch(String... runtimeArgs) {
        return createRuntimeWithMockedGlue(mock(PickleStepDefinitionMatch.class), true, mock(HookDefinition.class), HookType.After, Collections.<String>emptyList(), runtimeArgs);
    }

    private Runtime createRuntimeWithMockedGlue(PickleStepDefinitionMatch match, boolean isAmbiguous, HookDefinition hook,
                                                HookType hookType, List<String> featurePaths, String... runtimeArgs) {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final ClasspathResourceLoader resourceLoader = new ClasspathResourceLoader(classLoader);
        List<String> args = new ArrayList<String>(asList(runtimeArgs));
        if (!args.contains("-p")) {
            args.addAll(asList("-p", "null"));
        }
        args.addAll(featurePaths);
        this.runtimeOptions = new RuntimeOptions(args);
        this.backend = mock(Backend.class);
        this.templateGlue = mock(RuntimeGlue.class);
        this.localGlue = mock(RuntimeGlue.class);
        when(templateGlue.clone()).thenReturn(localGlue);
        mockMatch(localGlue, match, isAmbiguous);
        mockHook(localGlue, hook, hookType);

        return new Runtime(resourceLoader, classLoader, Arrays.asList(backend), runtimeOptions, templateGlue);
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

    private void runScenario(Runtime runtime, int stepCount) {
        List<PickleStep> steps = new ArrayList<PickleStep>(stepCount);
        for (int i = 0; i < stepCount; ++i) {
            steps.add(mock(PickleStep.class));
        }
        PickleEvent pickleEvent = new PickleEvent("uri", new Pickle("name", ENGLISH, steps, Collections.<PickleTag>emptyList(), asList(mock(PickleLocation.class))));

        runtime.getRunner().runPickle(pickleEvent);
    }

    private int stepCount(int stepCount) {
        return stepCount;
    }

    private TestCaseFinished testCaseFinishedWithStatus(Result.Type resultStatus) {
        return new TestCaseFinished(ANY_TIMESTAMP, mock(TestCase.class), new Result(resultStatus, null, null));
    }
}
