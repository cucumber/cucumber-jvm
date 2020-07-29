package io.cucumber.core.runtime;

import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.ParameterInfo;
import io.cucumber.core.backend.ScenarioScoped;
import io.cucumber.core.backend.TestCaseState;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.exception.CompositeCucumberException;
import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.gherkin.Step;
import io.cucumber.core.options.RuntimeOptionsBuilder;
import io.cucumber.core.plugin.FormatterSpy;
import io.cucumber.core.runner.StepDurationTimeService;
import io.cucumber.core.runner.TestBackendSupplier;
import io.cucumber.core.runner.TestHelper;
import io.cucumber.messages.Messages;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.Plugin;
import io.cucumber.plugin.event.HookType;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.StepDefinedEvent;
import io.cucumber.plugin.event.StepDefinition;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestStepFinished;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import static io.cucumber.core.runner.TestHelper.result;
import static java.time.Duration.ZERO;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RuntimeTest {

    private final static Instant ANY_INSTANT = Instant.ofEpochMilli(1234567890);

    private final EventBus bus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);

    @Test
    void with_passed_scenarios() {
        Runtime runtime = createStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Status.PASSED));

        assertThat(runtime.exitStatus(), is(equalTo((byte) 0x0)));
    }

    private Runtime createStrictRuntime() {
        return Runtime.builder()
                .withRuntimeOptions(
                    new RuntimeOptionsBuilder()
                            .build())
                .withEventBus(bus)
                .build();
    }

    private TestCaseFinished testCaseFinishedWithStatus(Status resultStatus) {
        return new TestCaseFinished(ANY_INSTANT, mock(TestCase.class), new Result(resultStatus, ZERO, null));
    }

    @Test
    void with_undefined_scenarios() {
        Runtime runtime = createStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Status.UNDEFINED));
        assertThat(runtime.exitStatus(), is(equalTo((byte) 0x1)));
    }

    @Test
    void with_pending_scenarios() {
        Runtime runtime = createStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Status.PENDING));

        assertThat(runtime.exitStatus(), is(equalTo((byte) 0x1)));
    }

    @Test
    void with_skipped_scenarios() {
        Runtime runtime = createNonStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Status.SKIPPED));

        assertThat(runtime.exitStatus(), is(equalTo((byte) 0x0)));
    }

    private Runtime createNonStrictRuntime() {
        return Runtime.builder()
                .withEventBus(bus)
                .build();
    }

    @Test
    void with_failed_scenarios() {
        Runtime runtime = createStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Status.FAILED));

        assertThat(runtime.exitStatus(), is(equalTo((byte) 0x1)));
    }

    @Test
    void with_ambiguous_scenarios() {
        Runtime runtime = createStrictRuntime();
        bus.send(testCaseFinishedWithStatus(Status.AMBIGUOUS));

        assertThat(runtime.exitStatus(), is(equalTo((byte) 0x1)));
    }

    @Test
    void should_pass_if_no_features_are_found() {
        Runtime runtime = Runtime.builder()
                .withRuntimeOptions(new RuntimeOptionsBuilder()
                        .build())
                .build();

        runtime.run();

        assertThat(runtime.exitStatus(), is(equalTo((byte) 0x0)));
    }

    @Test
    void should_make_scenario_name_available_to_hooks() {
        final Feature feature = TestFeatureParser.parse("path/test.feature",
            "Feature: feature name\n" +
                    "  Scenario: scenario name\n" +
                    "    Given first step\n" +
                    "    When second step\n" +
                    "    Then third step\n");
        final HookDefinition beforeHook = mock(HookDefinition.class);
        when(beforeHook.getLocation()).thenReturn("");
        when(beforeHook.getTagExpression()).thenReturn("");

        TestBackendSupplier testBackendSupplier = createTestBackendSupplier(feature, beforeHook);

        FeatureSupplier featureSupplier = new TestFeatureSupplier(feature);

        Runtime runtime = Runtime.builder()
                .withBackendSupplier(testBackendSupplier)
                .withFeatureSupplier(featureSupplier)
                .withEventBus(bus)
                .build();
        runtime.run();

        ArgumentCaptor<TestCaseState> capturedScenario = ArgumentCaptor.forClass(TestCaseState.class);
        verify(beforeHook).execute(capturedScenario.capture());
        assertThat(capturedScenario.getValue().getName(), is(equalTo("scenario name")));
    }

    private TestBackendSupplier createTestBackendSupplier(final Feature feature, final HookDefinition beforeHook) {
        return new TestBackendSupplier() {
            @Override
            public void loadGlue(Glue glue, List<URI> gluePaths) {
                for (Pickle child : feature.getPickles()) {
                    for (Step step : child.getSteps()) {
                        mockMatch(glue, step.getText());
                    }
                }
                mockHook(glue, beforeHook, HookType.BEFORE);
            }
        };
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

    @Test
    void should_call_formatter_for_two_scenarios_with_background() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
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

    private String runFeatureWithFormatterSpy(Feature feature, Map<String, Result> stepsToResult) {
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

    @Test
    void should_call_formatter_for_scenario_outline_with_two_examples_table_and_background() {
        Feature feature = TestFeatureParser.parse("path/test.feature", "" +
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
        Feature feature1 = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: feature name 1\n" +
                "  Scenario: scenario_1 name\n" +
                "    Given first step\n" +
                "  Scenario: scenario_2 name\n" +
                "    Given first step\n");

        Feature feature2 = TestFeatureParser.parse("path/test2.feature", "" +
                "Feature: feature name 2\n" +
                "  Scenario: scenario_2 name\n" +
                "    Given first step\n");

        Feature feature3 = TestFeatureParser.parse("path/test3.feature", "" +
                "Feature: feature name 3\n" +
                "  Scenario: scenario_3 name\n" +
                "    Given first step\n");

        FormatterSpy formatterSpy = new FormatterSpy();
        final List<Feature> features = Arrays.asList(feature1, feature2, feature3);

        Runtime.builder()
                .withFeatureSupplier(new TestFeatureSupplier(features))
                .withEventBus(bus)
                .withRuntimeOptions(new RuntimeOptionsBuilder().setThreads(features.size()).build())
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
        Feature feature1 = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: feature name 1\n" +
                "  Scenario: scenario_1 name\n" +
                "    Given first step\n" +
                "  Scenario: scenario_2 name\n" +
                "    Given first step\n");

        Feature feature2 = TestFeatureParser.parse("path/test2.feature", "" +
                "Feature: feature name 2\n" +
                "  Scenario: scenario_2 name\n" +
                "    Given first step\n");

        ConcurrentEventListener brokenEventListener = publisher -> publisher.registerHandlerFor(TestStepFinished.class,
            (TestStepFinished event) -> {
                throw new RuntimeException("This exception is expected");
            });

        Executable testMethod = () -> TestHelper.builder()
                .withFeatures(Arrays.asList(feature1, feature2))
                .withFormatterUnderTest(brokenEventListener)
                .withTimeServiceType(TestHelper.TimeServiceType.REAL_TIME)
                .withRuntimeArgs(new RuntimeOptionsBuilder().setThreads(2).build())
                .build()
                .run();
        CompositeCucumberException actualThrown = assertThrows(CompositeCucumberException.class, testMethod);
        assertThat(actualThrown.getMessage(), is(equalTo(
            "There were 3 exceptions:\n" +
                    "  java.lang.RuntimeException(This exception is expected)\n" +
                    "  java.lang.RuntimeException(This exception is expected)\n" +
                    "  java.lang.RuntimeException(This exception is expected)")));
    }

    @Test
    void should_interrupt_waiting_plugins() throws InterruptedException {
        final Feature feature1 = TestFeatureParser.parse("path/test.feature", "" +
                "Feature: feature name 1\n" +
                "  Scenario: scenario_1 name\n" +
                "    Given first step\n" +
                "  Scenario: scenario_2 name\n" +
                "    Given first step\n");

        final Feature feature2 = TestFeatureParser.parse("path/test2.feature", "" +
                "Feature: feature name 2\n" +
                "  Scenario: scenario_2 name\n" +
                "    Given first step\n");

        final CountDownLatch threadBlocked = new CountDownLatch(1);
        final CountDownLatch interruptHit = new CountDownLatch(1);

        final ConcurrentEventListener brokenEventListener = publisher -> publisher
                .registerHandlerFor(TestStepFinished.class, (TestStepFinished event) -> {
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
                .withRuntimeArgs(new RuntimeOptionsBuilder().setThreads(2).build())
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
        final Feature feature = TestFeatureParser.parse("test.feature", "" +
                "Feature: feature name\n" +
                "  Scenario: Run a scenario once\n" +
                "    Given global scoped\n" +
                "    And scenario scoped\n" +
                "  Scenario: Then do it again\n" +
                "    Given global scoped\n" +
                "    And scenario scoped\n" +
                "");

        final List<StepDefinition> stepDefinedEvents = new ArrayList<>();

        Plugin eventListener = (EventListener) publisher -> publisher.registerHandlerFor(StepDefinedEvent.class,
            (StepDefinedEvent event) -> {
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

        FeatureSupplier featureSupplier = () -> singletonList(feature);
        Runtime.builder()
                .withBackendSupplier(backendSupplier)
                .withAdditionalPlugins(eventListener)
                .withEventBus(new TimeServiceEventBus(new StepDurationTimeService(ZERO), UUID::randomUUID))
                .withFeatureSupplier(featureSupplier)
                .build()
                .run();

        assertThat(stepDefinedEvents.get(0).getPattern(), is(mockedStepDefinition.getPattern()));
        assertThat(stepDefinedEvents.get(1).getPattern(), is(mockedScenarioScopedStepDefinition.getPattern()));
        // Twice, once for each scenario
        assertThat(stepDefinedEvents.get(2).getPattern(), is(mockedStepDefinition.getPattern()));
        assertThat(stepDefinedEvents.get(3).getPattern(), is(mockedScenarioScopedStepDefinition.getPattern()));
        assertThat(stepDefinedEvents.size(), is(4));
    }

    @Test
    void emits_a_meta_message() {
        List<Messages.Envelope> messages = new ArrayList<>();
        EventListener listener = publisher -> publisher.registerHandlerFor(Messages.Envelope.class, messages::add);
        Runtime.builder()
                .withAdditionalPlugins(listener)
                .build()
                .run();

        Messages.Meta meta = messages.get(0).getMeta();
        assertThat(meta.getProtocolVersion(), matchesPattern("\\d+\\.\\d+\\.\\d+(-RC\\d+)?(-SNAPSHOT)?"));
        assertThat(meta.getImplementation().getName(), is("cucumber-jvm"));
        assertThat(meta.getImplementation().getVersion(), matchesPattern("\\d+\\.\\d+\\.\\d+(-RC\\d+)?(-SNAPSHOT)?"));
        assertThat(meta.getOs().getName(), matchesPattern(".+"));
        assertThat(meta.getCpu().getName(), matchesPattern(".+"));
    }

    private static final class MockedStepDefinition implements io.cucumber.core.backend.StepDefinition {

        @Override
        public void execute(Object[] args) {

        }

        @Override
        public List<ParameterInfo> parameterInfos() {
            return emptyList();
        }

        @Override
        public String getPattern() {
            return "global scoped";
        }

        @Override
        public boolean isDefinedAt(StackTraceElement stackTraceElement) {
            return false;
        }

        @Override
        public String getLocation() {
            return "mocked step definition";
        }

    }

    private static final class MockedScenarioScopedStepDefinition
            implements ScenarioScoped, io.cucumber.core.backend.StepDefinition {

        @Override
        public void execute(Object[] args) {

        }

        @Override
        public List<ParameterInfo> parameterInfos() {
            return emptyList();
        }

        @Override
        public String getPattern() {
            return "scenario scoped";
        }

        @Override
        public boolean isDefinedAt(StackTraceElement stackTraceElement) {
            return false;
        }

        @Override
        public String getLocation() {
            return "mocked scenario scoped step definition";
        }

    }

}
