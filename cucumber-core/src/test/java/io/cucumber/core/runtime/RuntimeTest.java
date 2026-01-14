package io.cucumber.core.runtime;

import io.cucumber.core.backend.CucumberBackendException;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.ParameterInfo;
import io.cucumber.core.backend.ScenarioScoped;
import io.cucumber.core.backend.StaticHookDefinition;
import io.cucumber.core.backend.StubStepDefinition;
import io.cucumber.core.backend.TestCaseState;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.exception.CompositeCucumberException;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.FeatureParserException;
import io.cucumber.core.options.RuntimeOptionsBuilder;
import io.cucumber.core.runner.StepDurationTimeService;
import io.cucumber.core.runner.TestBackendSupplier;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.Meta;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.Plugin;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.StepDefinedEvent;
import io.cucumber.plugin.event.StepDefinition;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import io.cucumber.plugin.event.TestStepFinished;
import io.cucumber.plugin.event.TestStepStarted;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;

import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import static java.time.Duration.ZERO;
import static java.time.Instant.EPOCH;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Disabled // TODO: Put tests into separate module
class RuntimeTest {

    private final static Instant ANY_INSTANT = Instant.ofEpochMilli(1234567890);

    private final EventBus bus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);

    @Test
    void with_passed_scenarios() {
        Runtime runtime = createRuntime();
        bus.send(testCaseFinishedWithStatus(Status.PASSED));

        assertThat(runtime.exitStatus(), is(equalTo((byte) 0x0)));
    }

    private Runtime createRuntime() {
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
        Runtime runtime = createRuntime();
        bus.send(testCaseFinishedWithStatus(Status.UNDEFINED));
        assertThat(runtime.exitStatus(), is(equalTo((byte) 0x1)));
    }

    @Test
    void with_pending_scenarios() {
        Runtime runtime = createRuntime();
        bus.send(testCaseFinishedWithStatus(Status.PENDING));

        assertThat(runtime.exitStatus(), is(equalTo((byte) 0x1)));
    }

    @Test
    void with_skipped_scenarios() {
        Runtime runtime = createRuntime();
        bus.send(testCaseFinishedWithStatus(Status.SKIPPED));

        assertThat(runtime.exitStatus(), is(equalTo((byte) 0x0)));
    }

    @Test
    void with_failed_scenarios() {
        Runtime runtime = createRuntime();
        bus.send(testCaseFinishedWithStatus(Status.FAILED));

        assertThat(runtime.exitStatus(), is(equalTo((byte) 0x1)));
    }

    @Test
    void with_ambiguous_scenarios() {
        Runtime runtime = createRuntime();
        bus.send(testCaseFinishedWithStatus(Status.AMBIGUOUS));

        assertThat(runtime.exitStatus(), is(equalTo((byte) 0x1)));
    }

    @Test
    void with_parse_error() {
        Runtime runtime = Runtime.builder()
                .withFeatureSupplier(() -> {
                    throw new FeatureParserException("oops");
                })
                .build();

        assertThrows(FeatureParserException.class, runtime::run);
    }

    @Test
    void should_pass_if_no_features_are_found() {
        Runtime runtime = Runtime.builder()
                .build();

        runtime.run();

        assertThat(runtime.exitStatus(), is(equalTo((byte) 0x0)));
    }

    @Test
    void should_make_scenario_name_available_to_hooks() {
        final Feature feature = TestFeatureParser.parse("path/test.feature",
            """
                    Feature: feature name
                      Scenario: scenario name
                        Given first step
                        When second step
                        Then third step
                    """);
        final HookDefinition beforeHook = mock(HookDefinition.class);
        when(beforeHook.getLocation()).thenReturn("");
        when(beforeHook.getTagExpression()).thenReturn("");

        FeatureSupplier featureSupplier = new StubFeatureSupplier(feature);

        Runtime runtime = Runtime.builder()
                .withFeatureSupplier(featureSupplier)
                .withBackendSupplier(new StubBackendSupplier(
                    singletonList(beforeHook),
                    asList(
                        new StubStepDefinition("first step"),
                        new StubStepDefinition("second step"),
                        new StubStepDefinition("third step")),
                    emptyList()))
                .build();
        runtime.run();

        ArgumentCaptor<TestCaseState> capturedScenario = ArgumentCaptor.forClass(TestCaseState.class);
        verify(beforeHook).execute(capturedScenario.capture());
        assertThat(capturedScenario.getValue().getName(), is(equalTo("scenario name")));
    }

    @Test
    void should_call_formatter_for_two_scenarios_with_background() {
        Feature feature = TestFeatureParser.parse("path/test.feature", """
                Feature: feature name
                  Background: background
                    Given first step
                  Scenario: scenario_1 name
                    When second step
                    Then third step
                  Scenario: scenario_2 name
                    Then second step
                """);

        FormatterSpy formatterSpy = new FormatterSpy();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(formatterSpy)
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("first step"),
                    new StubStepDefinition("second step"),
                    new StubStepDefinition("third step")))
                .build()
                .run();

        assertThat(formatterSpy.toString(),
            is(equalTo("""
                    TestRun started
                      TestCase started
                        TestStep started
                        TestStep finished
                        TestStep started
                        TestStep finished
                        TestStep started
                        TestStep finished
                      TestCase finished
                      TestCase started
                        TestStep started
                        TestStep finished
                        TestStep started
                        TestStep finished
                      TestCase finished
                    TestRun finished
                    """)));
    }

    @Test
    void should_call_formatter_for_scenario_outline_with_two_examples_table_and_background() {
        Feature feature = TestFeatureParser.parse("path/test.feature", """
                Feature: feature name
                  Background: background
                    Given first step
                  Scenario Outline: scenario outline name
                    When <x> step
                    Then <y> step
                    Examples: examples 1 name
                      |   x    |   y   |
                      | second | third |
                      | second | third |
                    Examples: examples 2 name
                      |   x    |   y   |
                      | second | third |
                """);

        FormatterSpy formatterSpy = new FormatterSpy();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature))
                .withAdditionalPlugins(formatterSpy)
                .withEventBus(new TimeServiceEventBus(Clock.fixed(EPOCH, ZoneId.of("UTC")), UUID::randomUUID))
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("first step"),
                    new StubStepDefinition("second step"),
                    new StubStepDefinition("third step")))
                .build()
                .run();

        assertThat(formatterSpy.toString(),
            is(equalTo("""
                    TestRun started
                      TestCase started
                        TestStep started
                        TestStep finished
                        TestStep started
                        TestStep finished
                        TestStep started
                        TestStep finished
                      TestCase finished
                      TestCase started
                        TestStep started
                        TestStep finished
                        TestStep started
                        TestStep finished
                        TestStep started
                        TestStep finished
                      TestCase finished
                      TestCase started
                        TestStep started
                        TestStep finished
                        TestStep started
                        TestStep finished
                        TestStep started
                        TestStep finished
                      TestCase finished
                    TestRun finished
                    """)));
    }

    @Test
    void should_call_formatter_with_correct_sequence_of_events_when_running_in_parallel() {
        Feature feature1 = TestFeatureParser.parse("path/test.feature", """
                Feature: feature name 1
                  Scenario: scenario_1 name
                    Given first step
                  Scenario: scenario_2 name
                    Given first step
                """);

        Feature feature2 = TestFeatureParser.parse("path/test2.feature", """
                Feature: feature name 2
                  Scenario: scenario_2 name
                    Given first step
                """);

        Feature feature3 = TestFeatureParser.parse("path/test3.feature", """
                Feature: feature name 3
                  Scenario: scenario_3 name
                    Given first step
                """);

        FormatterSpy formatterSpy = new FormatterSpy();
        Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature1, feature2, feature3))
                .withAdditionalPlugins(formatterSpy)
                .withBackendSupplier(new StubBackendSupplier(
                    new StubStepDefinition("first step")))
                .withRuntimeOptions(new RuntimeOptionsBuilder().setThreads(3).build())
                .build()
                .run();

        String formatterOutput = formatterSpy.toString();

        assertThat(formatterOutput,
            is(equalTo("""
                    TestRun started
                      TestCase started
                        TestStep started
                        TestStep finished
                      TestCase finished
                      TestCase started
                        TestStep started
                        TestStep finished
                      TestCase finished
                      TestCase started
                        TestStep started
                        TestStep finished
                      TestCase finished
                      TestCase started
                        TestStep started
                        TestStep finished
                      TestCase finished
                    TestRun finished
                    """)));
    }

    @Test
    void should_fail_on_event_listener_exception_when_running_in_parallel() {
        Feature feature1 = TestFeatureParser.parse("path/test.feature", """
                Feature: feature name 1
                  Scenario: scenario_1 name
                    Given first step
                  Scenario: scenario_2 name
                    Given first step
                """);

        Feature feature2 = TestFeatureParser.parse("path/test2.feature", """
                Feature: feature name 2
                  Scenario: scenario_2 name
                    Given first step
                """);

        ConcurrentEventListener brokenEventListener = publisher -> publisher.registerHandlerFor(TestStepFinished.class,
            (TestStepFinished event) -> {
                throw new RuntimeException("This exception is expected");
            });

        Executable testMethod = () -> Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature1, feature2))
                .withAdditionalPlugins(brokenEventListener)
                .withRuntimeOptions(new RuntimeOptionsBuilder().setThreads(2).build())
                .build()
                .run();
        CompositeCucumberException actualThrown = assertThrows(CompositeCucumberException.class, testMethod);
        assertThat(actualThrown.getMessage(),
            is(equalTo("There were 3 exceptions. The details are in the stacktrace below.")));
        assertThat(actualThrown.getSuppressed(), is(arrayWithSize(3)));
    }

    @Test
    void should_fail_on_event_listener_exception_at_test_run_started() {
        RuntimeException expectedException = new RuntimeException("This exception is expected");
        ConcurrentEventListener brokenEventListener = publisher -> publisher.registerHandlerFor(TestRunStarted.class,
            (TestRunStarted event) -> {
                throw expectedException;
            });

        Executable testMethod = () -> Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier())
                .withAdditionalPlugins(brokenEventListener)
                .build()
                .run();
        RuntimeException actualThrown = assertThrows(RuntimeException.class, testMethod);
        assertThat(actualThrown, equalTo(expectedException));
    }

    @Test
    void should_fail_on_event_listener_exception_at_test_run_finished() {
        RuntimeException expectedException = new RuntimeException("This exception is expected");
        ConcurrentEventListener brokenEventListener = publisher -> publisher.registerHandlerFor(TestRunFinished.class,
            (TestRunFinished event) -> {
                throw expectedException;
            });

        Executable testMethod = () -> Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier())
                .withAdditionalPlugins(brokenEventListener)
                .build()
                .run();
        RuntimeException actualThrown = assertThrows(RuntimeException.class, testMethod);
        assertThat(actualThrown, equalTo(expectedException));
    }

    @Test
    void should_fail_on_exception_invoking_after_all_hook() {
        RuntimeException expectedException = new RuntimeException("This exception is expected");
        CucumberBackendException backendException = new CucumberBackendException("failed", expectedException);
        MockedStaticHookDefinition mockedStaticHookDefinition = new MockedStaticHookDefinition(() -> {
            throw backendException;
        });

        BackendSupplier backendSupplier = new TestBackendSupplier() {
            @Override
            public void loadGlue(Glue glue, List<URI> gluePaths) {
                glue.addAfterAllHook(mockedStaticHookDefinition);
            }
        };

        Executable testMethod = () -> Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier())
                .withBackendSupplier(backendSupplier)
                .build()
                .run();
        CucumberException actualThrown = assertThrows(CucumberException.class, testMethod);
        assertThat(actualThrown.getCause(), equalTo(backendException));
    }

    @Test
    void should_interrupt_waiting_plugins() throws InterruptedException {
        final Feature feature1 = TestFeatureParser.parse("path/test.feature", """
                Feature: feature name 1
                  Scenario: scenario_1 name
                    Given first step
                  Scenario: scenario_2 name
                    Given first step
                """);

        final Feature feature2 = TestFeatureParser.parse("path/test2.feature", """
                Feature: feature name 2
                  Scenario: scenario_2 name
                    Given first step
                """);

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

        Thread thread = new Thread(() -> Runtime.builder()
                .withFeatureSupplier(new StubFeatureSupplier(feature1, feature2))
                .withAdditionalPlugins(brokenEventListener)
                .withRuntimeOptions(new RuntimeOptionsBuilder().setThreads(2).build())
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
        final Feature feature = TestFeatureParser.parse("test.feature", """
                Feature: feature name
                  Scenario: Run a scenario once
                    Given global scoped
                    And scenario scoped
                  Scenario: Then do it again
                    Given global scoped
                    And scenario scoped
                """);

        final List<StepDefinition> stepDefinedEvents = new ArrayList<>();

        Plugin eventListener = (EventListener) publisher -> publisher.registerHandlerFor(StepDefinedEvent.class,
            (StepDefinedEvent event) -> {
                stepDefinedEvents.add(event.getStepDefinition());
            });

        final MockedStepDefinition mockedStepDefinition = new MockedStepDefinition();
        final MockedScenarioScopedStepDefinition mockedScenarioScopedStepDefinition = new MockedScenarioScopedStepDefinition();

        BackendSupplier backendSupplier = new TestBackendSupplier() {

            private @Nullable Glue glue;

            @Override
            public void loadGlue(Glue glue, List<URI> gluePaths) {
                this.glue = glue;
                glue.addStepDefinition(mockedStepDefinition);
            }

            @Override
            public void buildWorld() {
                Objects.requireNonNull(glue);
                glue.addStepDefinition(mockedScenarioScopedStepDefinition);
            }
        };

        FeatureSupplier featureSupplier = new StubFeatureSupplier(feature);
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
        List<Envelope> messages = new ArrayList<>();
        EventListener listener = publisher -> publisher.registerHandlerFor(Envelope.class, messages::add);
        Runtime.builder()
                .withAdditionalPlugins(listener)
                .build()
                .run();

        Meta meta = messages.get(0).getMeta().get();
        assertThat(meta.getProtocolVersion(), matchesPattern("\\d+\\.\\d+\\.\\d+(-RC\\d+)?(-SNAPSHOT)?"));
        assertThat(meta.getImplementation().getName(), is("cucumber-jvm"));
        assertThat(meta.getImplementation().getVersion().get(),
            matchesPattern("\\d+\\.\\d+\\.\\d+(-RC\\d+)?(-SNAPSHOT)?"));
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

    @SuppressWarnings("deprecation")
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

    private final static class MockedStaticHookDefinition implements StaticHookDefinition {

        private final Runnable runnable;

        private MockedStaticHookDefinition(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void execute() {
            runnable.run();
        }

        @Override
        public int getOrder() {
            return 0;
        }

        @Override
        public boolean isDefinedAt(StackTraceElement stackTraceElement) {
            return false;
        }

        @Override
        public String getLocation() {
            return "mocked hook definition definition";
        }
    }

    private static final class FormatterSpy implements EventListener {

        private final StringBuilder calls = new StringBuilder();

        @Override
        public void setEventPublisher(EventPublisher publisher) {
            publisher.registerHandlerFor(TestRunStarted.class, event -> calls.append("TestRun started\n"));
            publisher.registerHandlerFor(TestCaseStarted.class, event -> calls.append("  TestCase started\n"));
            publisher.registerHandlerFor(TestCaseFinished.class, event -> calls.append("  TestCase finished\n"));
            publisher.registerHandlerFor(TestStepStarted.class, event -> calls.append("    TestStep started\n"));
            publisher.registerHandlerFor(TestStepFinished.class, event -> calls.append("    TestStep finished\n"));
            publisher.registerHandlerFor(TestRunFinished.class, event -> calls.append("TestRun finished\n"));
        }

        @Override
        public String toString() {
            return calls.toString();
        }

    }

}
