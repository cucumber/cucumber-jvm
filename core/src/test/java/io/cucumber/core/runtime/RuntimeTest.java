package io.cucumber.core.runtime;

import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.ParameterInfo;
import io.cucumber.core.backend.Scenario;
import io.cucumber.core.event.HookType;
import io.cucumber.core.event.Result;
import io.cucumber.core.event.Status;
import io.cucumber.core.event.StepDefinedEvent;
import io.cucumber.core.event.StepDefinition;
import io.cucumber.core.event.TestCase;
import io.cucumber.core.event.TestCaseFinished;
import io.cucumber.core.event.TestStepFinished;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.exception.CompositeCucumberException;
import io.cucumber.core.feature.CucumberFeature;
import io.cucumber.core.feature.CucumberPickle;
import io.cucumber.core.feature.CucumberStep;
import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.io.ResourceLoader;
import io.cucumber.core.io.TestClasspathResourceLoader;
import io.cucumber.core.options.CommandlineOptionsParser;
import io.cucumber.core.plugin.ConcurrentEventListener;
import io.cucumber.core.plugin.EventListener;
import io.cucumber.core.plugin.FormatterBuilder;
import io.cucumber.core.plugin.FormatterSpy;
import io.cucumber.core.plugin.Plugin;
import io.cucumber.core.runner.ScenarioScoped;
import io.cucumber.core.runner.StepDurationTimeService;
import io.cucumber.core.runner.TestBackendSupplier;
import io.cucumber.core.runner.TestHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static io.cucumber.core.runner.TestHelper.result;
import static java.time.Duration.ZERO;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

class RuntimeTest {

    private final static Instant ANY_INSTANT = Instant.ofEpochMilli(1234567890);

    private final EventBus bus = new TimeServiceEventBus(Clock.systemUTC());

    @Test
    void runs_feature_with_json_formatter() {
        final CucumberFeature feature = TestFeatureParser.parse("test.feature", "" +
            "Feature: feature name\n" +
            "  Background: background name\n" +
            "    Given b\n" +
            "  Scenario: scenario name\n" +
            "    When s\n");
        StringBuilder out = new StringBuilder();

        Plugin jsonFormatter = FormatterBuilder.jsonFormatter(out);
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        BackendSupplier backendSupplier = new TestBackendSupplier() {
            @Override
            public void loadGlue(Glue glue, List<URI> gluePaths) {

            }
        };
        FeatureSupplier featureSupplier = new TestFeatureSupplier(bus, feature);
        Runtime.builder()
            .withBackendSupplier(backendSupplier)
            .withAdditionalPlugins(jsonFormatter)
            .withResourceLoader(TestClasspathResourceLoader.create(classLoader))
            .withEventBus(new TimeServiceEventBus(Clock.fixed(Instant.EPOCH, ZoneId.of("UTC"))))
            .withFeatureSupplier(featureSupplier)
            .build()
            .run();

        String expected = "" +
            "[\n" +
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
            "        \"start_timestamp\": \"1970-01-01T00:00:00.000Z\",\n" +
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
            "    \"uri\": \"file:test.feature\",\n" +
            "    \"tags\": []\n" +
            "  }\n" +
            "]";
        assertThat(out.toString(), sameJSONAs(expected));
    }

    @Test
    void strict_with_passed_scenarios() {
        Runtime runtime = createStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Status.PASSED));

        assertThat(runtime.exitStatus(), is(equalTo((byte) 0x0)));
    }

    @Test
    void non_strict_with_passed_scenarios() {
        Runtime runtime = createNonStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Status.PASSED));

        assertThat(runtime.exitStatus(), is(equalTo((byte) 0x0)));
    }

    @Test
    void non_strict_with_undefined_scenarios() {
        Runtime runtime = createNonStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Status.UNDEFINED));
        assertThat(runtime.exitStatus(), is(equalTo((byte) 0x0)));
    }

    @Test
    void strict_with_undefined_scenarios() {
        Runtime runtime = createStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Status.UNDEFINED));
        assertThat(runtime.exitStatus(), is(equalTo((byte) 0x1)));
    }

    @Test
    void strict_with_pending_scenarios() {
        Runtime runtime = createStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Status.PENDING));

        assertThat(runtime.exitStatus(), is(equalTo((byte) 0x1)));
    }

    @Test
    void non_strict_with_pending_scenarios() {
        Runtime runtime = createNonStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Status.PENDING));

        assertThat(runtime.exitStatus(), is(equalTo((byte) 0x0)));
    }

    @Test
    void non_strict_with_skipped_scenarios() {
        Runtime runtime = createNonStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Status.SKIPPED));

        assertThat(runtime.exitStatus(), is(equalTo((byte) 0x0)));
    }

    @Test
    void strict_with_skipped_scenarios() {
        Runtime runtime = createNonStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Status.SKIPPED));

        assertThat(runtime.exitStatus(), is(equalTo((byte) 0x0)));
    }

    @Test
    void non_strict_with_failed_scenarios() {
        Runtime runtime = createNonStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Status.FAILED));

        assertThat(runtime.exitStatus(), is(equalTo((byte) 0x1)));
    }

    @Test
    void strict_with_failed_scenarios() {
        Runtime runtime = createStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Status.FAILED));

        assertThat(runtime.exitStatus(), is(equalTo((byte) 0x1)));
    }

    @Test
    void non_strict_with_ambiguous_scenarios() {
        Runtime runtime = createNonStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Status.AMBIGUOUS));

        assertThat(runtime.exitStatus(), is(equalTo((byte) 0x1)));
    }

    @Test
    void strict_with_ambiguous_scenarios() {
        Runtime runtime = createStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Status.AMBIGUOUS));

        assertThat(runtime.exitStatus(), is(equalTo((byte) 0x1)));
    }

    @Test
    void should_pass_if_no_features_are_found() {
        ResourceLoader resourceLoader = createResourceLoaderThatFindsNoFeatures();
        Runtime runtime = createStrictRuntime(resourceLoader);

        runtime.run();

        assertThat(runtime.exitStatus(), is(equalTo((byte) 0x0)));
    }

    @Test
    void should_make_scenario_name_available_to_hooks() throws Throwable {
        final CucumberFeature feature = TestFeatureParser.parse("path/test.feature",
            "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Given first step\n" +
                "    When second step\n" +
                "    Then third step\n");
        final HookDefinition beforeHook = mock(HookDefinition.class);
        when(beforeHook.getTagExpression()).thenReturn("");

        TestBackendSupplier testBackendSupplier = createTestBackendSupplier(feature, beforeHook);

        FeatureSupplier featureSupplier = new TestFeatureSupplier(bus, feature);

        Runtime runtime = Runtime.builder()
            .withBackendSupplier(testBackendSupplier)
            .withFeatureSupplier(featureSupplier)
            .withEventBus(bus)
            .build();
        runtime.run();

        ArgumentCaptor<Scenario> capturedScenario = ArgumentCaptor.forClass(Scenario.class);
        verify(beforeHook).execute(capturedScenario.capture());
        assertThat(capturedScenario.getValue().getName(), is(equalTo("scenario name")));
    }

    private TestBackendSupplier createTestBackendSupplier(final CucumberFeature feature, final HookDefinition beforeHook) {
        return new TestBackendSupplier() {
            @Override
            public void loadGlue(Glue glue, List<URI> gluePaths) {
                for (CucumberPickle child : feature.getPickles()) {
                    for (CucumberStep step : child.getSteps()) {
                        mockMatch(glue, step.getText());
                    }
                }
                mockHook(glue, beforeHook, HookType.BEFORE);
            }
        };
    }

    @Test
    void should_call_formatter_for_two_scenarios_with_background() {
        CucumberFeature feature = TestFeatureParser.parse("path/test.feature", "" +
            "Feature: feature name\n" +
            "  Background: background\n" +
            "    Given first step\n" +
            "  Scenario: scenario_1 name\n" +
            "    When second step\n" +
            "    Then third step\n" +
            "  Scenario: scenario_2 name\n" +
            "    Then second step\n");
        Map<String, Result> stepsToResult = new HashMap<>();
        stepsToResult.put("first step", result("passed"));
        stepsToResult.put("second step", result("passed"));
        stepsToResult.put("third step", result("passed"));

        String formatterOutput = runFeatureWithFormatterSpy(feature, stepsToResult);

        assertThat(formatterOutput,
            is(equalTo("" +
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
                "TestRun finished\n")));
    }

    @Test
    void should_call_formatter_for_scenario_outline_with_two_examples_table_and_background() {
        CucumberFeature feature = TestFeatureParser.parse("path/test.feature", "" +
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
        Map<String, Result> stepsToResult = new HashMap<>();
        stepsToResult.put("first step", result("passed"));
        stepsToResult.put("second step", result("passed"));
        stepsToResult.put("third step", result("passed"));

        String formatterOutput = runFeatureWithFormatterSpy(feature, stepsToResult);

        assertThat(formatterOutput,
            is(equalTo("" +
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
                "TestRun finished\n")));
    }

    @Test
    void should_call_formatter_with_correct_sequence_of_events_when_running_in_parallel() {
        CucumberFeature feature1 = TestFeatureParser.parse("path/test.feature", "" +
            "Feature: feature name 1\n" +
            "  Scenario: scenario_1 name\n" +
            "    Given first step\n" +
            "  Scenario: scenario_2 name\n" +
            "    Given first step\n");

        CucumberFeature feature2 = TestFeatureParser.parse("path/test2.feature", "" +
            "Feature: feature name 2\n" +
            "  Scenario: scenario_2 name\n" +
            "    Given first step\n");

        CucumberFeature feature3 = TestFeatureParser.parse("path/test3.feature", "" +
            "Feature: feature name 3\n" +
            "  Scenario: scenario_3 name\n" +
            "    Given first step\n");

        FormatterSpy formatterSpy = new FormatterSpy();
        final List<CucumberFeature> features = Arrays.asList(feature1, feature2, feature3);

        Runtime.builder()
            .withFeatureSupplier(new TestFeatureSupplier(bus, features))
            .withEventBus(bus)
            .withRuntimeOptions(
                new CommandlineOptionsParser()
                    .parse("--threads", String.valueOf(features.size()))
                    .build()
            )
            .withAdditionalPlugins(formatterSpy)
            .withBackendSupplier(new TestHelper.TestHelperBackendSupplier(features))
            .build()
            .run();

        String formatterOutput = formatterSpy.toString();

        assertThat(formatterOutput,
            is(equalTo("" +
                "TestCase started\n" +
                "  TestStep started\n" +
                "  TestStep finished\n" +
                "TestCase finished\n" +
                "TestCase started\n" +
                "  TestStep started\n" +
                "  TestStep finished\n" +
                "TestCase finished\n" +
                "TestCase started\n" +
                "  TestStep started\n" +
                "  TestStep finished\n" +
                "TestCase finished\n" +
                "TestCase started\n" +
                "  TestStep started\n" +
                "  TestStep finished\n" +
                "TestCase finished\n" +
                "TestRun finished\n")));
    }

    @Test
    void should_fail_on_event_listener_exception_when_running_in_parallel() {
        CucumberFeature feature1 = TestFeatureParser.parse("path/test.feature", "" +
            "Feature: feature name 1\n" +
            "  Scenario: scenario_1 name\n" +
            "    Given first step\n" +
            "  Scenario: scenario_2 name\n" +
            "    Given first step\n");

        CucumberFeature feature2 = TestFeatureParser.parse("path/test2.feature", "" +
            "Feature: feature name 2\n" +
            "  Scenario: scenario_2 name\n" +
            "    Given first step\n");

        ConcurrentEventListener brokenEventListener = publisher -> publisher.registerHandlerFor(TestStepFinished.class, (TestStepFinished event) -> {
            throw new RuntimeException("boom");
        });

        Executable testMethod = () -> TestHelper.builder()
            .withFeatures(Arrays.asList(feature1, feature2))
            .withFormatterUnderTest(brokenEventListener)
            .withTimeServiceType(TestHelper.TimeServiceType.REAL_TIME)
            .withRuntimeArgs("--threads", "2")
            .build()
            .run();
        CompositeCucumberException actualThrown = assertThrows(CompositeCucumberException.class, testMethod);
        assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo(
            "There were 3 exceptions:\n  java.lang.RuntimeException(boom)\n  java.lang.RuntimeException(boom)\n  java.lang.RuntimeException(boom)"
        )));
    }

    @Test
    void should_interrupt_waiting_plugins() throws InterruptedException {
        final CucumberFeature feature1 = TestFeatureParser.parse("path/test.feature", "" +
            "Feature: feature name 1\n" +
            "  Scenario: scenario_1 name\n" +
            "    Given first step\n" +
            "  Scenario: scenario_2 name\n" +
            "    Given first step\n");

        final CucumberFeature feature2 = TestFeatureParser.parse("path/test2.feature", "" +
            "Feature: feature name 2\n" +
            "  Scenario: scenario_2 name\n" +
            "    Given first step\n");

        final CountDownLatch threadBlocked = new CountDownLatch(1);
        final CountDownLatch interruptHit = new CountDownLatch(1);

        final ConcurrentEventListener brokenEventListener = publisher -> publisher.registerHandlerFor(TestStepFinished.class, (TestStepFinished event) -> {
            try {
                threadBlocked.countDown();
                HOURS.sleep(1);
            } catch (InterruptedException ignored) {
                interruptHit.countDown();
            }
        });

        Thread thread = new Thread(() -> TestHelper.builder()
            .withFeatures(Arrays.asList(feature1, feature2))
            .withFormatterUnderTest(brokenEventListener)
            .withTimeServiceType(TestHelper.TimeServiceType.REAL_TIME)
            .withRuntimeArgs("--threads", "2")
            .build()
            .run());

        thread.start();
        threadBlocked.await(1, SECONDS);
        thread.interrupt();
        interruptHit.await(1, SECONDS);
        assertThat(interruptHit.getCount(), is(equalTo(0L)));
    }

    @Test
    void generates_events_for_glue_and_scenario_scoped_glue() {
        final CucumberFeature feature = TestFeatureParser.parse("test.feature", "" +
            "Feature: feature name\n" +
            "  Scenario: Run a scenario once\n" +
            "    Given global scoped\n" +
            "    And scenario scoped\n" +
            "  Scenario: Then do it again\n" +
            "    Given global scoped\n" +
            "    And scenario scoped\n" +
            "");

        final List<StepDefinition> stepDefinedEvents = new ArrayList<>();

        Plugin eventListener = (EventListener) publisher -> publisher.registerHandlerFor(StepDefinedEvent.class, (StepDefinedEvent event) -> {
            stepDefinedEvents.add(event.getStepDefinition());
        });


        final MockedStepDefinition mockedStepDefinition = new MockedStepDefinition();
        final MockedScenarioScopedStepDefinition mockedScenarioScopedStepDefinition = new MockedScenarioScopedStepDefinition();

        BackendSupplier backendSupplier = new TestBackendSupplier() {

            private Glue glue;

            @Override
            public void loadGlue(Glue glue, List<URI> gluePaths) {
                this.glue = glue;
                glue.addStepDefinition(mockedStepDefinition);
            }

            @Override
            public void buildWorld() {
                glue.addStepDefinition(mockedScenarioScopedStepDefinition);
            }
        };

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        FeatureSupplier featureSupplier = () -> singletonList(feature);
        Runtime.builder()
            .withBackendSupplier(backendSupplier)
            .withAdditionalPlugins(eventListener)
            .withResourceLoader(TestClasspathResourceLoader.create(classLoader))
            .withEventBus(new TimeServiceEventBus(new StepDurationTimeService(ZERO)))
            .withFeatureSupplier(featureSupplier)
            .build()
            .run();


        assertThat(stepDefinedEvents, contains(
            mockedStepDefinition,
            mockedScenarioScopedStepDefinition,
            // Twice, once for each scenario
            mockedStepDefinition,
            mockedScenarioScopedStepDefinition
        ));

        for (StepDefinition stepDefinedEvent : stepDefinedEvents) {
            if (stepDefinedEvent instanceof MockedScenarioScopedStepDefinition) {
                MockedScenarioScopedStepDefinition mocked = (MockedScenarioScopedStepDefinition) stepDefinedEvent;
                assertTrue(mocked.disposed, "Scenario scoped step definition should be disposed of");
            }
        }

    }

    private String runFeatureWithFormatterSpy(CucumberFeature feature, Map<String, Result> stepsToResult) {
        FormatterSpy formatterSpy = new FormatterSpy();

        TestHelper.builder()
            .withFeatures(feature)
            .withStepsToResult(stepsToResult)
            .withFormatterUnderTest(formatterSpy)
            .withTimeServiceType(TestHelper.TimeServiceType.REAL_TIME)
            .build()
            .run();

        return formatterSpy.toString();
    }

    private ResourceLoader createResourceLoaderThatFindsNoFeatures() {
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        when(resourceLoader.resources(any(URI.class), eq(".feature"))).thenReturn(Collections.emptyList());
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
        BackendSupplier backendSupplier = new TestBackendSupplier() {
            @Override
            public void loadGlue(Glue glue, List<URI> gluePaths) {

            }
        };

        return Runtime.builder()
            .withRuntimeOptions(
                new CommandlineOptionsParser()
                    .parse(runtimeArgs)
                    .build()
            )
            .withClassLoader(classLoader)
            .withResourceLoader(resourceLoader)
            .withBackendSupplier(backendSupplier)
            .withEventBus(bus)
            .build();
    }

    private void mockMatch(Glue glue, String text) {
        io.cucumber.core.backend.StepDefinition stepDefinition = new StubStepDefinition(text);
        glue.addStepDefinition(stepDefinition);
    }

    private void mockHook(Glue glue, HookDefinition hook, HookType hookType) {
        switch (hookType) {
            case BEFORE:
                glue.addBeforeHook(hook);
                return;
            case AFTER:
                glue.addAfterHook(hook);
                return;
            case AFTER_STEP:
                glue.addAfterStepHook(hook);
                return;
            case BEFORE_STEP:
                glue.addBeforeStepHook(hook);
                return;
            default:
                throw new IllegalArgumentException(hookType.name());
        }
    }

    private TestCaseFinished testCaseFinishedWithStatus(Status resultStatus) {
        return new TestCaseFinished(ANY_INSTANT, mock(TestCase.class), new Result(resultStatus, ZERO, null));
    }

    private static final class MockedStepDefinition implements io.cucumber.core.backend.StepDefinition {

        @Override
        public String getLocation() {
            return "mocked step definition";
        }

        @Override
        public List<ParameterInfo> parameterInfos() {
            return emptyList();
        }

        @Override
        public void execute(Object[] args) {

        }

        @Override
        public boolean isDefinedAt(StackTraceElement stackTraceElement) {
            return false;
        }

        @Override
        public String getPattern() {
            return "global scoped";
        }

    }

    private static final class MockedScenarioScopedStepDefinition implements io.cucumber.core.backend.StepDefinition, ScenarioScoped {

        boolean disposed;

        @Override
        public void disposeScenarioScope() {
            this.disposed = true;
        }

        @Override
        public String getLocation() {
            return "mocked scenario scoped step definition";
        }

        @Override
        public List<ParameterInfo> parameterInfos() {
            return emptyList();
        }

        @Override
        public void execute(Object[] args) {

        }

        @Override
        public boolean isDefinedAt(StackTraceElement stackTraceElement) {
            return false;
        }

        @Override
        public String getPattern() {
            return "scenario scoped";
        }

    }

}
