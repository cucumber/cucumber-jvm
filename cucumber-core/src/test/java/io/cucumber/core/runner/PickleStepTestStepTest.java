package io.cucumber.core.runner;

import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.StubPendingException;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.messages.types.Envelope;
import io.cucumber.plugin.event.EventHandler;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCaseEvent;
import io.cucumber.plugin.event.TestStepFinished;
import io.cucumber.plugin.event.TestStepStarted;
import org.junit.jupiter.api.Test;
import org.opentest4j.TestAbortedException;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.cucumber.core.backend.Status.FAILED;
import static io.cucumber.core.backend.Status.PASSED;
import static io.cucumber.core.backend.Status.PENDING;
import static io.cucumber.core.backend.Status.SKIPPED;
import static io.cucumber.plugin.event.HookType.AFTER_STEP;
import static io.cucumber.plugin.event.HookType.BEFORE_STEP;
import static java.time.Duration.ofMillis;
import static java.time.Instant.ofEpochMilli;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PickleStepTestStepTest {

    private final Feature feature = TestFeatureParser.parse("" +
            "Feature: Test feature\n" +
            "  Scenario: Test scenario\n" +
            "     Given I have 4 cukes in my belly\n");
    private final Pickle pickle = feature.getPickles().get(0);
    private final TestCase testCase = new TestCase(UUID.randomUUID(), Collections.emptyList(), Collections.emptyList(),
        Collections.emptyList(), pickle, false);
    private MockEventBus bus = new MockEventBus();
    private final TestCaseState state = new TestCaseState(bus, UUID.randomUUID(), testCase);
    private PickleStepDefinitionMatch definitionMatch;
    private CoreHookDefinition afterHookDefinition;
    private CoreHookDefinition beforeHookDefinition;
    private PickleStepTestStep step;

    private void buildStep(
            RuntimeException beforeHookException, RuntimeException afterHookException, Throwable stepException
    ) {
        beforeHookDefinition = CoreHookDefinition.create(new MockHookDefinition(beforeHookException), UUID::randomUUID);
        afterHookDefinition = CoreHookDefinition.create(new MockHookDefinition(afterHookException), UUID::randomUUID);
        definitionMatch = new MockPickleStepDefinitionMatch(stepException);
        step = new PickleStepTestStep(
            UUID.randomUUID(),
            URI.create("file:path/to.feature"),
            pickle.getSteps().get(0),
            singletonList(new HookTestStep(UUID.randomUUID(), BEFORE_STEP,
                new HookDefinitionMatch(beforeHookDefinition))),
            singletonList(new HookTestStep(UUID.randomUUID(), AFTER_STEP,
                new HookDefinitionMatch(afterHookDefinition))),
            definitionMatch);
    }

    @Test
    void run_wraps_run_step_in_test_step_started_and_finished_events() throws Throwable {
        buildStep(null, null, null);

        step.run(testCase, bus, state, ExecutionMode.RUN);

        List<Object> events = bus.events.stream()
                .filter(event -> !(event instanceof Envelope))
                .collect(Collectors.toList());
        assertInstanceOf(TestStepStarted.class, events.get(0));
        Object stepDefinitionEvent = events.get(3);
        assertInstanceOf(PickleStepDefinitionMatchEvent.class, stepDefinitionEvent);
        assertEquals("runStep", ((PickleStepDefinitionMatchEvent) stepDefinitionEvent).method);
        assertEquals(state, ((PickleStepDefinitionMatchEvent) stepDefinitionEvent).state);
        assertInstanceOf(TestStepFinished.class, events.get(events.size() - 1));
    }

    @Test
    void run_does_dry_run_step_when_dry_run_steps_is_true() throws Throwable {
        buildStep(null, null, null);

        step.run(testCase, bus, state, ExecutionMode.DRY_RUN);

        List<Object> events = bus.events.stream()
                .filter(event -> !(event instanceof Envelope))
                .collect(Collectors.toList());
        assertInstanceOf(TestStepStarted.class, events.get(0));
        Object stepDefinitionEvent = events.get(3);
        assertInstanceOf(PickleStepDefinitionMatchEvent.class, stepDefinitionEvent);
        assertEquals("dryRunStep", ((PickleStepDefinitionMatchEvent) stepDefinitionEvent).method);
        assertEquals(state, ((PickleStepDefinitionMatchEvent) stepDefinitionEvent).state);
        assertInstanceOf(TestStepFinished.class, events.get(events.size() - 1));
    }

    @Test
    void run_skips_step_when_skip_step_is_true() throws Throwable {
        buildStep(null, null, null);

        step.run(testCase, bus, state, ExecutionMode.SKIP);

        List<Object> events = bus.events.stream()
                .filter(event -> !(event instanceof Envelope))
                .collect(Collectors.toList());
        assertInstanceOf(TestStepStarted.class, events.get(0));
        assertFalse(bus.events.stream().anyMatch(event -> event instanceof PickleStepDefinitionMatchEvent));
        assertInstanceOf(TestStepFinished.class, events.get(events.size() - 1));
    }

    @Test
    void result_is_passed_run_when_step_definition_does_not_throw_exception() {
        buildStep(null, null, null);

        ExecutionMode nextExecutionMode = step.run(testCase, bus, state, ExecutionMode.RUN);

        assertThat(nextExecutionMode, is(ExecutionMode.RUN));
        assertThat(state.getStatus(), is(equalTo(PASSED)));
    }

    @Test
    void result_is_skipped_when_skip_step_is_not_run_all() {
        buildStep(null, null, null);

        ExecutionMode nextExecutionMode = step.run(testCase, bus, state, ExecutionMode.SKIP);

        assertThat(nextExecutionMode, is(ExecutionMode.SKIP));
        assertThat(state.getStatus(), is(equalTo(SKIPPED)));
    }

    @Test
    void result_is_skipped_when_before_step_hook_does_not_pass() {
        buildStep(new TestAbortedException(), null, null);

        ExecutionMode nextExecutionMode = step.run(testCase, bus, state, ExecutionMode.RUN);

        assertThat(nextExecutionMode, is(ExecutionMode.SKIP));
        assertThat(state.getStatus(), is(equalTo(SKIPPED)));
    }

    @Test
    void step_execution_is_skipped_when_before_step_hook_does_not_pass() throws Throwable {
        buildStep(new TestAbortedException(), null, null);

        step.run(testCase, bus, state, ExecutionMode.RUN);

        assertFalse(bus.events.stream().anyMatch(event -> event instanceof PickleStepDefinitionMatchEvent));
    }

    @Test
    void result_is_result_from_hook_when_before_step_hook_does_not_pass() {
        RuntimeException exception = new RuntimeException();
        buildStep(exception, null, null);

        ExecutionMode nextExecutionMode = step.run(testCase, bus, state, ExecutionMode.RUN);

        assertThat(nextExecutionMode, is(ExecutionMode.SKIP));
        assertThat(state.getStatus(), is(equalTo(FAILED)));
        List<TestCaseEvent> events = bus.events.stream()
                .filter(event -> event instanceof TestCaseEvent)
                .map(event -> (TestCaseEvent) event)
                .collect(Collectors.toList());
        assertEquals(6, events.size());
        Result result = ((TestStepFinished) events.get(1)).getResult();
        assertEquals(Status.FAILED, result.getStatus());
        assertEquals(exception, result.getError());
    }

    @Test
    void result_is_result_from_step_when_step_hook_does_not_pass() throws Throwable {
        RuntimeException runtimeException = new RuntimeException();
        buildStep(null, null, runtimeException);

        ExecutionMode nextExecutionMode = step.run(testCase, bus, state, ExecutionMode.RUN);

        assertThat(nextExecutionMode, is(ExecutionMode.SKIP));
        assertThat(state.getStatus(), is(equalTo(FAILED)));
        List<TestCaseEvent> events = bus.events.stream()
                .filter(event -> event instanceof TestCaseEvent)
                .map(event -> (TestCaseEvent) event)
                .collect(Collectors.toList());
        assertEquals(6, events.size());
        Result result = ((TestStepFinished) events.get(3)).getResult();
        assertEquals(Status.FAILED, result.getStatus());
        assertEquals(runtimeException, result.getError());
    }

    @Test
    void result_is_result_from_hook_when_after_step_hook_does_not_pass() {
        RuntimeException exception = new RuntimeException();
        buildStep(null, exception, null);

        ExecutionMode nextExecutionMode = step.run(testCase, bus, state, ExecutionMode.RUN);

        assertThat(nextExecutionMode, is(ExecutionMode.SKIP));
        assertThat(state.getStatus(), is(equalTo(FAILED)));
        List<TestCaseEvent> events = bus.events.stream()
                .filter(event -> event instanceof TestCaseEvent)
                .map(event -> (TestCaseEvent) event)
                .collect(Collectors.toList());
        assertEquals(6, events.size());
        Result result = ((TestStepFinished) events.get(5)).getResult();
        assertEquals(Status.FAILED, result.getStatus());
        assertEquals(exception, result.getError());
    }

    @Test
    void after_step_hook_is_run_when_before_step_hook_does_not_pass() {
        buildStep(new RuntimeException(), null, null);

        step.run(testCase, bus, state, ExecutionMode.RUN);

        assertTrue(((MockHookDefinition) afterHookDefinition.delegate).executed);
    }

    @Test
    void after_step_hook_is_run_when_step_does_not_pass() throws Throwable {
        buildStep(null, null, new Exception());

        step.run(testCase, bus, state, ExecutionMode.RUN);

        assertTrue(((MockHookDefinition) afterHookDefinition.delegate).executed);
    }

    @Test
    void after_step_hook_scenario_contains_step_failure_when_step_does_not_pass() throws Throwable {
        Throwable expectedError = new TestAbortedException("oops");
        buildStep(null, null, expectedError);

        step.run(testCase, bus, state, ExecutionMode.RUN);

        assertThat(((MockHookDefinition) afterHookDefinition.delegate).receivedError, is(expectedError));
    }

    @Test
    void after_step_hook_scenario_contains_before_step_hook_failure_when_before_step_hook_does_not_pass() {
        RuntimeException expectedError = new TestAbortedException("oops");
        buildStep(expectedError, null, null);

        step.run(testCase, bus, state, ExecutionMode.RUN);

        assertThat(((MockHookDefinition) afterHookDefinition.delegate).receivedError, is(expectedError));
    }

    @Test
    void result_is_skipped_when_step_definition_throws_assumption_violated_exception() throws Throwable {
        buildStep(null, null, new TestAbortedException());

        ExecutionMode nextExecutionMode = step.run(testCase, bus, state, ExecutionMode.RUN);

        assertThat(nextExecutionMode, is(ExecutionMode.SKIP));
        assertThat(state.getStatus(), is(equalTo(SKIPPED)));
    }

    @Test
    void result_is_failed_when_step_definition_throws_exception() throws Throwable {
        buildStep(null, null, new RuntimeException());

        ExecutionMode nextExecutionMode = step.run(testCase, bus, state, ExecutionMode.RUN);

        assertThat(nextExecutionMode, is(ExecutionMode.SKIP));
        assertThat(state.getStatus(), is(equalTo(FAILED)));
    }

    @Test
    void result_is_pending_when_step_definition_throws_pending_exception() throws Throwable {
        buildStep(null, null, new StubPendingException());

        ExecutionMode nextExecutionMode = step.run(testCase, bus, state, ExecutionMode.RUN);

        assertThat(nextExecutionMode, is(ExecutionMode.SKIP));
        assertThat(state.getStatus(), is(equalTo(PENDING)));
    }

    @Test
    void step_execution_time_is_measured() {
        Feature feature = TestFeatureParser.parse("" +
                "Feature: Test feature\n" +
                "  Scenario: Test scenario\n" +
                "     Given I have 4 cukes in my belly\n");
        TestStep step = new PickleStepTestStep(
            UUID.randomUUID(),
            URI.create("file:path/to.feature"),
            feature.getPickles().get(0).getSteps().get(0),
            definitionMatch);
        bus = new MockEventBus(ofEpochMilli(234L), ofEpochMilli(1234L));

        step.run(testCase, bus, state, ExecutionMode.RUN);

        List<TestCaseEvent> events = bus.events.stream()
                .filter(event -> event instanceof TestCaseEvent)
                .map(event -> (TestCaseEvent) event)
                .collect(Collectors.toList());
        assertEquals(2, events.size());
        TestStepStarted started = (TestStepStarted) events.get(0);
        TestStepFinished finished = (TestStepFinished) events.get(1);
        assertAll(
            () -> assertThat(started.getInstant(), is(equalTo(ofEpochMilli(234L)))),
            () -> assertThat(finished.getInstant(), is(equalTo(ofEpochMilli(1234L)))),
            () -> assertThat(finished.getResult().getDuration(), is(equalTo(ofMillis(1000L)))));
    }

    private static class MockEventBus implements EventBus {
        final List<Object> events = new ArrayList<>();
        final Queue<Instant> instants;

        public MockEventBus(Instant... instants) {
            this.instants = new ArrayDeque<>(Arrays.asList(instants));
        }

        @Override
        public Instant getInstant() {
            Instant instant = instants.poll();
            return instant != null ? instant : Instant.now();
        }

        @Override
        public UUID generateId() {
            return null;
        }

        @Override
        public <T> void send(T event) {
            events.add(event);
        }

        @Override
        public <T> void sendAll(Iterable<T> queue) {
            queue.forEach(events::add);
        }

        @Override
        public <T> void registerHandlerFor(Class<T> eventType, EventHandler<T> handler) {

        }

        @Override
        public <T> void removeHandlerFor(Class<T> eventType, EventHandler<T> handler) {

        }
    }

    private static class PickleStepDefinitionMatchEvent {
        Object target;
        String method;
        io.cucumber.core.backend.TestCaseState state;
        public PickleStepDefinitionMatchEvent(
                Object target, String method, io.cucumber.core.backend.TestCaseState state
        ) {
            this.target = target;
            this.method = method;
            this.state = state;
        }
    }

    private class MockPickleStepDefinitionMatch extends PickleStepDefinitionMatch {
        private final Throwable stepException;

        MockPickleStepDefinitionMatch(Throwable stepException) {
            super(new ArrayList<>(), new StubStepDefinition(""), null, null);
            this.stepException = stepException;
        }

        @Override
        public void runStep(io.cucumber.core.backend.TestCaseState state) throws Throwable {
            bus.send(new PickleStepDefinitionMatchEvent(this, "runStep", state));
            if (stepException != null) {
                throw stepException;
            }
        }

        @Override
        public void dryRunStep(io.cucumber.core.backend.TestCaseState state) throws Throwable {
            bus.send(new PickleStepDefinitionMatchEvent(this, "dryRunStep", state));
            if (stepException != null) {
                throw stepException;
            }
        }
    }

    private static class MockHookDefinition implements HookDefinition {
        private final RuntimeException executeException;
        boolean executed;
        Throwable receivedError;

        public MockHookDefinition(RuntimeException executeException) {
            this.executeException = executeException;
        }

        @Override
        public void execute(io.cucumber.core.backend.TestCaseState state) {
            executed = true;
            receivedError = ((TestCaseState) state).getError();
            if (executeException != null) {
                throw executeException;
            }
        }

        @Override
        public String getTagExpression() {
            return "";
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
            return null;
        }
    }
}
