package io.cucumber.core.runtime;

import gherkin.ast.ScenarioDefinition;
import gherkin.ast.Step;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleTag;
import io.cucumber.core.api.Scenario;
import io.cucumber.core.api.event.ConcurrentEventListener;
import io.cucumber.core.api.event.EventHandler;
import io.cucumber.core.api.event.EventListener;
import io.cucumber.core.api.event.EventPublisher;
import io.cucumber.core.api.event.HookType;
import io.cucumber.core.api.event.Result;
import io.cucumber.core.api.event.StepDefinedEvent;
import io.cucumber.core.api.event.TestCase;
import io.cucumber.core.api.event.TestCaseFinished;
import io.cucumber.core.api.event.TestStepFinished;
import io.cucumber.core.api.plugin.Plugin;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.api.event.StepDefinition;
import io.cucumber.core.event.EventBus;
import io.cucumber.core.exception.CompositeCucumberException;
import io.cucumber.core.io.Resource;
import io.cucumber.core.io.ResourceLoader;
import io.cucumber.core.io.TestClasspathResourceLoader;
import io.cucumber.core.feature.CucumberFeature;
import io.cucumber.core.options.CommandlineOptionsParser;
import io.cucumber.core.plugin.FormatterBuilder;
import io.cucumber.core.plugin.FormatterSpy;
import io.cucumber.core.runner.ScenarioScoped;
import io.cucumber.core.runner.StepDurationTimeService;
import io.cucumber.core.runner.TestBackendSupplier;
import io.cucumber.core.runner.TestHelper;
import io.cucumber.core.runner.TimeServiceEventBus;
import io.cucumber.core.stepexpression.TypeRegistry;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static io.cucumber.core.runner.TestHelper.feature;
import static io.cucumber.core.runner.TestHelper.result;
import static java.time.Duration.ZERO;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

public class RuntimeTest {
    private final static Instant ANY_INSTANT = Instant.ofEpochMilli(1234567890);

    private final TypeRegistry TYPE_REGISTRY = new TypeRegistry(Locale.ENGLISH);
    private final EventBus bus = new TimeServiceEventBus(Clock.systemUTC());

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
    public void should_make_scenario_name_available_to_hooks() throws Throwable {
        final CucumberFeature feature = TestHelper.feature("path/test.feature",
            "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Given first step\n" +
                "    When second step\n" +
                "    Then third step\n");
        final HookDefinition beforeHook = mock(HookDefinition.class);
        when(beforeHook.matches(ArgumentMatchers.<PickleTag>anyCollection())).thenReturn(true);

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
        assertEquals("scenario name", capturedScenario.getValue().getName());
    }

    private TestBackendSupplier createTestBackendSupplier(final CucumberFeature feature, final HookDefinition beforeHook) {
        return new TestBackendSupplier() {
            @Override
            public void loadGlue(Glue glue, List<URI> gluePaths) {
                for (ScenarioDefinition child : feature.getGherkinFeature().getFeature().getChildren()) {
                    for (Step step : child.getSteps()) {
                        mockMatch(glue, step.getText());
                    }
                }
                mockHook(glue, beforeHook, HookType.Before);
            }
        };
    }

    @Test
    public void should_call_formatter_for_two_scenarios_with_background() {
        CucumberFeature feature = TestHelper.feature("path/test.feature", "" +
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
    public void should_call_formatter_for_scenario_outline_with_two_examples_table_and_background() {
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
        Map<String, Result> stepsToResult = new HashMap<>();
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
    public void should_call_formatter_with_correct_sequence_of_events_when_running_in_parallel() {
        CucumberFeature feature1 = TestHelper.feature("path/test.feature", "" +
            "Feature: feature name 1\n" +
            "  Scenario: scenario_1 name\n" +
            "    Given first step\n" +
            "  Scenario: scenario_2 name\n" +
            "    Given first step\n");

        CucumberFeature feature2 = TestHelper.feature("path/test2.feature", "" +
            "Feature: feature name 2\n" +
            "  Scenario: scenario_2 name\n" +
            "    Given first step\n");

        CucumberFeature feature3 = TestHelper.feature("path/test3.feature", "" +
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

        assertEquals("" +
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
            "TestRun finished\n", formatterOutput);
    }

    @Test
    public void should_fail_on_event_listener_exception_when_running_in_parallel() {
        CucumberFeature feature1 = TestHelper.feature("path/test.feature", "" +
            "Feature: feature name 1\n" +
            "  Scenario: scenario_1 name\n" +
            "    Given first step\n" +
            "  Scenario: scenario_2 name\n" +
            "    Given first step\n");

        CucumberFeature feature2 = TestHelper.feature("path/test2.feature", "" +
            "Feature: feature name 2\n" +
            "  Scenario: scenario_2 name\n" +
            "    Given first step\n");

        ConcurrentEventListener brokenEventListener = new ConcurrentEventListener() {
            @Override
            public void setEventPublisher(EventPublisher publisher) {
                publisher.registerHandlerFor(TestStepFinished.class, new EventHandler<TestStepFinished>() {
                    @Override
                    public void receive(TestStepFinished event) {
                        throw new RuntimeException("boom");
                    }
                });
            }
        };

        expectedException.expect(CompositeCucumberException.class);
        expectedException.expectMessage("There were 3 exceptions");

        TestHelper.builder()
            .withFeatures(Arrays.asList(feature1, feature2))
            .withFormatterUnderTest(brokenEventListener)
            .withTimeServiceType(TestHelper.TimeServiceType.REAL_TIME)
            .withRuntimeArgs("--threads", "2")
            .build()
            .run();

    }

    @Test
    public void should_interrupt_waiting_plugins() throws InterruptedException {
        final CucumberFeature feature1 = TestHelper.feature("path/test.feature", "" +
            "Feature: feature name 1\n" +
            "  Scenario: scenario_1 name\n" +
            "    Given first step\n" +
            "  Scenario: scenario_2 name\n" +
            "    Given first step\n");

        final CucumberFeature feature2 = TestHelper.feature("path/test2.feature", "" +
            "Feature: feature name 2\n" +
            "  Scenario: scenario_2 name\n" +
            "    Given first step\n");

        final CountDownLatch threadBlocked = new CountDownLatch(1);
        final CountDownLatch interruptHit = new CountDownLatch(1);

        final ConcurrentEventListener brokenEventListener = new ConcurrentEventListener() {
            @Override
            public void setEventPublisher(EventPublisher publisher) {
                publisher.registerHandlerFor(TestStepFinished.class, new EventHandler<TestStepFinished>() {
                    @Override
                    public void receive(TestStepFinished event) {
                        try {
                            threadBlocked.countDown();
                            HOURS.sleep(1);
                        } catch (InterruptedException ignored) {
                            interruptHit.countDown();
                        }
                    }
                });
            }
        };

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                TestHelper.builder()
                    .withFeatures(Arrays.asList(feature1, feature2))
                    .withFormatterUnderTest(brokenEventListener)
                    .withTimeServiceType(TestHelper.TimeServiceType.REAL_TIME)
                    .withRuntimeArgs("--threads", "2")
                    .build()
                    .run();

            }
        });

        thread.start();
        threadBlocked.await(1, SECONDS);
        thread.interrupt();
        interruptHit.await(1, SECONDS);
        assertEquals(0, interruptHit.getCount());
    }

    @Test
    public void generates_events_for_glue_and_scenario_scoped_glue() {
        final CucumberFeature feature = feature("test.feature", "" +
            "Feature: feature name\n" +
            "  Scenario: Run a scenario once\n" +
            "    Given global scoped\n" +
            "    And scenario scoped\n" +
            "  Scenario: Then do it again\n" +
            "    Given global scoped\n" +
            "    And scenario scoped\n" +
            "");

        final List<StepDefinition> stepDefinedEvents = new ArrayList<>();

        Plugin eventListener = new EventListener() {
            @Override
            public void setEventPublisher(EventPublisher publisher) {
                publisher.registerHandlerFor(StepDefinedEvent.class, new EventHandler<StepDefinedEvent>() {
                    @Override
                    public void receive(StepDefinedEvent event) {
                        stepDefinedEvents.add(event.stepDefinition);
                    }
                });
            }
        };


        final List<StepDefinition> definedStepDefinitions = new ArrayList<>();

        BackendSupplier backendSupplier = new TestBackendSupplier() {

            private Glue glue;

            @Override
            public void loadGlue(Glue glue, List<URI> gluePaths) {
                this.glue = glue;
                final io.cucumber.core.backend.StepDefinition mockedStepDefinition = new MockedStepDefinition();
                definedStepDefinitions.add(mockedStepDefinition);
                glue.addStepDefinition(mockedStepDefinition);
            }

            @Override
            public void buildWorld() {
                final io.cucumber.core.backend.StepDefinition mockedScenarioScopedStepDefinition = new MockedScenarioScopedStepDefinition();
                definedStepDefinitions.add(mockedScenarioScopedStepDefinition);
                glue.addStepDefinition(mockedScenarioScopedStepDefinition);
            }
        };

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        FeatureSupplier featureSupplier = new FeatureSupplier() {
            @Override
            public List<CucumberFeature> get() {
                return singletonList(feature);
            }
        };
        Runtime.builder()
            .withBackendSupplier(backendSupplier)
            .withAdditionalPlugins(eventListener)
            .withResourceLoader(TestClasspathResourceLoader.create(classLoader))
            .withEventBus(new TimeServiceEventBus(new StepDurationTimeService(ZERO)))
            .withFeatureSupplier(featureSupplier)
            .build()
            .run();

        assertThat(stepDefinedEvents, equalTo(definedStepDefinitions));

        for (StepDefinition stepDefinedEvent : stepDefinedEvents) {
            if (stepDefinedEvent instanceof MockedScenarioScopedStepDefinition) {
                MockedScenarioScopedStepDefinition mocked = (MockedScenarioScopedStepDefinition) stepDefinedEvent;
                assertTrue("Scenario scoped step definition should be disposed of", mocked.disposed);
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
        when(resourceLoader.resources(any(URI.class), eq(".feature"))).thenReturn(Collections.<Resource>emptyList());
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

    private Runtime createRuntimeWithMockedGlue(final HookDefinition hook,
                                                final HookType hookType,
                                                final CucumberFeature feature,
                                                String... runtimeArgs) {
        TestBackendSupplier testBackendSupplier = new TestBackendSupplier() {
            @Override
            public void loadGlue(Glue glue, List<URI> gluePaths) {
                for (ScenarioDefinition child : feature.getGherkinFeature().getFeature().getChildren()) {
                    for (Step step : child.getSteps()) {
                        mockMatch(glue, step.getText());
                    }
                }
                mockHook(glue, hook, hookType);
            }
        };

        FeatureSupplier featureSupplier = new FeatureSupplier() {
            @Override
            public List<CucumberFeature> get() {
                return singletonList(feature);
            }
        };

        return Runtime.builder()
            .withRuntimeOptions(
                new CommandlineOptionsParser()
                    .parse(runtimeArgs)
                    .build()
            )
            .withBackendSupplier(testBackendSupplier)
            .withFeatureSupplier(featureSupplier)
            .build();

    }

    private void mockMatch(Glue glue, String text) {
        io.cucumber.core.backend.StepDefinition stepDefinition = new StubStepDefinition(text, TYPE_REGISTRY);
        glue.addStepDefinition(stepDefinition);
    }

    private void mockHook(Glue glue, HookDefinition hook, HookType hookType) {
        switch (hookType) {
            case Before:
                glue.addBeforeHook(hook);
                return;
            case After:
                glue.addAfterHook(hook);
                return;
            case AfterStep:
                glue.addAfterStepHook(hook);
                return;
            case BeforeStep:
                glue.addBeforeStepHook(hook);
                return;
            default:
                throw new IllegalArgumentException(hookType.name());
        }
    }

    private TestCaseFinished testCaseFinishedWithStatus(Result.Type resultStatus) {
        return new TestCaseFinished(ANY_INSTANT, mock(TestCase.class), new Result(resultStatus, ZERO, null));
    }

    private static final class MockedStepDefinition implements io.cucumber.core.backend.StepDefinition {

        @Override
        public List<io.cucumber.core.stepexpression.Argument> matchedArguments(PickleStep step) {
            return step.getText().equals(getPattern()) ? new ArrayList<>() : null;
        }

        @Override
        public String getLocation(boolean detail) {
            return "mocked step definition";
        }

        @Override
        public Integer getParameterCount() {
            return 0;
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
        public List<io.cucumber.core.stepexpression.Argument> matchedArguments(PickleStep step) {
            return step.getText().equals(getPattern()) ? new ArrayList<>() : null;
        }

        @Override
        public String getLocation(boolean detail) {
            return "mocked scenario scoped step definition";
        }

        @Override
        public Integer getParameterCount() {
            return 0;
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
