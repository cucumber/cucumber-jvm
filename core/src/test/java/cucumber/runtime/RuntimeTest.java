package cucumber.runtime;

import cucumber.api.HookType;
import cucumber.api.Plugin;
import cucumber.api.Result;
import cucumber.api.Scenario;
import cucumber.api.StepDefinitionReporter;
import cucumber.api.TestCase;
import cucumber.api.event.*;
import cucumber.api.event.EventListener;
import cucumber.runner.EventBus;
import cucumber.runner.TestBackendSupplier;
import cucumber.runner.TestHelper;
import cucumber.runner.TimeService;
import cucumber.runner.TimeServiceEventBus;
import cucumber.runner.TimeServiceStub;
import cucumber.runtime.formatter.FormatterBuilder;
import cucumber.runtime.formatter.FormatterSpy;
import cucumber.runtime.io.ClasspathResourceLoader;
import cucumber.runtime.io.Resource;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.model.CucumberFeature;
import gherkin.ast.ScenarioDefinition;
import gherkin.ast.Step;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleTag;
import io.cucumber.stepexpression.Argument;
import io.cucumber.stepexpression.TypeRegistry;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;

import java.net.URI;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static cucumber.runner.TestHelper.feature;
import static cucumber.runner.TestHelper.result;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

public class RuntimeTest {
    private final static long ANY_TIMESTAMP = 1234567890;

    private final TypeRegistry TYPE_REGISTRY = new TypeRegistry(Locale.ENGLISH);
    private final EventBus bus = new TimeServiceEventBus(TimeService.SYSTEM);

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
        FeatureSupplier featureSupplier = new FeatureSupplier() {
            @Override
            public List<CucumberFeature> get() {
                return singletonList(feature);
            }
        };
        Runtime.builder()
            .withBackendSupplier(backendSupplier)
            .withAdditionalPlugins(jsonFormatter)
            .withResourceLoader(new ClasspathResourceLoader(classLoader))
            .withEventBus(new TimeServiceEventBus(new TimeServiceStub(0)))
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
    public void reports_step_definitions_to_plugin() {
        ResourceLoader resourceLoader = mock(ResourceLoader.class);
        final StubStepDefinition stepDefinition = new StubStepDefinition("some pattern", new TypeRegistry(Locale.ENGLISH));
        TestBackendSupplier testBackendSupplier = new TestBackendSupplier() {
            @Override
            public void loadGlue(Glue glue, List<URI> gluePaths) {
                glue.addStepDefinition(stepDefinition);
            }
        };

        Runtime.builder()
            .withResourceLoader(resourceLoader)
            .withArgs("--plugin", "cucumber.runtime.RuntimeTest$StepdefsPrinter")
            .withBackendSupplier(testBackendSupplier)
            .build()
            .run();

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
    public void should_make_scenario_name_available_to_hooks() throws Throwable {
        CucumberFeature feature = TestHelper.feature("path/test.feature",
            "Feature: feature name\n" +
                "  Scenario: scenario name\n" +
                "    Given first step\n" +
                "    When second step\n" +
                "    Then third step\n");
        HookDefinition beforeHook = mock(HookDefinition.class);
        when(beforeHook.matches(ArgumentMatchers.<PickleTag>anyCollection())).thenReturn(true);

        Runtime runtime = createRuntimeWithMockedGlue(beforeHook, HookType.Before, feature);
        runtime.run();

        ArgumentCaptor<Scenario> capturedScenario = ArgumentCaptor.forClass(Scenario.class);
        verify(beforeHook).execute(capturedScenario.capture());
        assertEquals("scenario name", capturedScenario.getValue().getName());
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

        Map<String, Result> stepsToResult = new HashMap<>();
        stepsToResult.put("first step", result("passed"));

        FormatterSpy formatterSpy = new FormatterSpy();
        final List<CucumberFeature> features = Arrays.asList(feature1, feature2, feature3);

        TestHelper.builder()
            .withFeatures(features)
            .withStepsToResult(stepsToResult)
            .withFormatterUnderTest(formatterSpy)
            .withTimeServiceType(TestHelper.TimeServiceType.REAL_TIME)
            .withRuntimeArgs("--threads", String.valueOf(features.size()))
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

        final AtomicBoolean threadBlocked = new AtomicBoolean(false);
        final AtomicBoolean interruptHit = new AtomicBoolean(false);

        final ConcurrentEventListener brokenEventListener = new ConcurrentEventListener() {
            @Override
            public void setEventPublisher(EventPublisher publisher) {
                publisher.registerHandlerFor(TestStepFinished.class, new EventHandler<TestStepFinished>() {
                    @Override
                    public void receive(TestStepFinished event) {
                        try {
                            threadBlocked.set(true);
                            HOURS.sleep(1);
                        } catch (InterruptedException ignored) {
                            interruptHit.set(true);
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
        do {
            SECONDS.sleep(5);
        } while (!threadBlocked.get());
        thread.interrupt();
        MINUTES.timedJoin(thread, 1);
        assertTrue(interruptHit.get());
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
                final StepDefinition mockedStepDefinition = new MockedStepDefinition();
                definedStepDefinitions.add(mockedStepDefinition);
                glue.addStepDefinition(mockedStepDefinition);
            }

            @Override
            public void buildWorld() {
                final StepDefinition mockedScenarioScopedStepDefinition = new MockedScenarioScopedStepDefinition();
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
            .withResourceLoader(new ClasspathResourceLoader(classLoader))
            .withEventBus(new TimeServiceEventBus(new TimeServiceStub(0)))
            .withFeatureSupplier(featureSupplier)
            .build()
            .run();

        assertThat(stepDefinedEvents, equalTo(definedStepDefinitions));

        for (StepDefinition stepDefinedEvent : stepDefinedEvents) {
            if(stepDefinedEvent instanceof MockedScenarioScopedStepDefinition){
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
        BackendSupplier backendSupplier = new TestBackendSupplier(){
            @Override
            public void loadGlue(Glue glue, List<URI> gluePaths) {

            }
        };

        return Runtime.builder()
            .withArgs(runtimeArgs)
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
            .withArgs(runtimeArgs)
            .withBackendSupplier(testBackendSupplier)
            .withFeatureSupplier(featureSupplier)
            .build();

    }

    private void mockMatch(Glue glue, String text) {
        StepDefinition stepDefinition = new StubStepDefinition(text, TYPE_REGISTRY);
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
                glue.addBeforeHook(hook);
                return;
            default:
                throw new IllegalArgumentException(hookType.name());
        }
    }

    private TestCaseFinished testCaseFinishedWithStatus(Result.Type resultStatus) {
        return new TestCaseFinished(ANY_TIMESTAMP, ANY_TIMESTAMP, mock(TestCase.class), new Result(resultStatus, 0L, null));
    }

    private static final class MockedStepDefinition implements StepDefinition {

        @Override
        public List<Argument> matchedArguments(PickleStep step) {
            return step.getText().equals(getPattern()) ? new ArrayList<Argument>() : null;
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
        public void execute(Object[] args) throws Throwable {

        }

        @Override
        public boolean isDefinedAt(StackTraceElement stackTraceElement) {
            return false;
        }

        @Override
        public String getPattern() {
            return "global scoped";
        }

        @Override
        public boolean isScenarioScoped() {
            return true;
        }
    }

    private static final class MockedScenarioScopedStepDefinition implements StepDefinition, ScenarioScoped {

        boolean disposed;

        @Override
        public void disposeScenarioScope() {
            this.disposed = true;
        }

        @Override
        public List<Argument> matchedArguments(PickleStep step) {
            return step.getText().equals(getPattern()) ? new ArrayList<Argument>() : null;
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

        @Override
        public boolean isScenarioScoped() {
            return true;
        }
    }
}
