package io.cucumber.core.runner;

import io.cucumber.core.backend.StubPendingException;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCaseEvent;
import io.cucumber.plugin.event.TestStepFinished;
import io.cucumber.plugin.event.TestStepStarted;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.opentest4j.TestAbortedException;

import java.net.URI;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static io.cucumber.core.backend.Status.FAILED;
import static io.cucumber.core.backend.Status.PASSED;
import static io.cucumber.core.backend.Status.PENDING;
import static io.cucumber.core.backend.Status.SKIPPED;
import static io.cucumber.plugin.event.HookType.AFTER_STEP;
import static io.cucumber.plugin.event.HookType.BEFORE_STEP;
import static java.time.Duration.ZERO;
import static java.time.Duration.ofMillis;
import static java.time.Instant.ofEpochMilli;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PickleStepTestStepTest {

    private final Feature feature = TestFeatureParser.parse("" +
            "Feature: Test feature\n" +
            "  Scenario: Test scenario\n" +
            "     Given I have 4 cukes in my belly\n");
    private final Pickle pickle = feature.getPickles().get(0);
    private final TestCase testCase = new TestCase(UUID.randomUUID(), Collections.emptyList(), Collections.emptyList(),
        Collections.emptyList(), pickle, false);
    private final EventBus bus = mock(EventBus.class);
    private final UUID testExecutionId = UUID.randomUUID();
    private final TestCaseState state = new TestCaseState(bus, testExecutionId, testCase);
    private final PickleStepDefinitionMatch definitionMatch = mock(PickleStepDefinitionMatch.class);
    private final CoreHookDefinition afterHookDefinition = mock(CoreHookDefinition.class);
    private final HookTestStep afterHook = new HookTestStep(UUID.randomUUID(), AFTER_STEP,
        new HookDefinitionMatch(afterHookDefinition));
    private final CoreHookDefinition beforeHookDefinition = mock(CoreHookDefinition.class);
    private final HookTestStep beforeHook = new HookTestStep(UUID.randomUUID(), BEFORE_STEP,
        new HookDefinitionMatch(beforeHookDefinition));
    private final PickleStepTestStep step = new PickleStepTestStep(
        UUID.randomUUID(),
        URI.create("file:path/to.feature"),
        pickle.getSteps().get(0),
        singletonList(beforeHook),
        singletonList(afterHook),
        definitionMatch);

    @BeforeEach
    void init() {
        Mockito.when(bus.getInstant()).thenReturn(Instant.now());
    }

    @Test
    void run_wraps_run_step_in_test_step_started_and_finished_events() throws Throwable {
        step.run(testCase, bus, state, ExecutionMode.RUN);

        InOrder order = inOrder(bus, definitionMatch);
        order.verify(bus).send(isA(TestStepStarted.class));
        order.verify(definitionMatch).runStep(state);
        order.verify(bus).send(isA(TestStepFinished.class));
    }

    @Test
    void run_does_dry_run_step_when_dry_run_steps_is_true() throws Throwable {
        step.run(testCase, bus, state, ExecutionMode.DRY_RUN);

        InOrder order = inOrder(bus, definitionMatch);
        order.verify(bus).send(isA(TestStepStarted.class));
        order.verify(definitionMatch).dryRunStep(state);
        order.verify(bus).send(isA(TestStepFinished.class));
    }

    @Test
    void run_skips_step_when_dry_run_and_skip_step_is_true() throws Throwable {
        step.run(testCase, bus, state, ExecutionMode.SKIP);

        InOrder order = inOrder(bus, definitionMatch);
        order.verify(bus).send(isA(TestStepStarted.class));
        order.verify(definitionMatch, never()).dryRunStep(state);
        order.verify(bus).send(isA(TestStepFinished.class));
    }

    @Test
    void run_skips_step_when_skip_step_is_true() throws Throwable {
        step.run(testCase, bus, state, ExecutionMode.SKIP);

        InOrder order = inOrder(bus, definitionMatch);
        order.verify(bus).send(isA(TestStepStarted.class));
        order.verify(definitionMatch, never()).dryRunStep(state);
        order.verify(bus).send(isA(TestStepFinished.class));
    }

    @Test
    void result_is_passed_run_when_step_definition_does_not_throw_exception() {
        ExecutionMode nextExecutionMode = step.run(testCase, bus, state, ExecutionMode.RUN);
        assertThat(nextExecutionMode, is(ExecutionMode.RUN));
        assertThat(state.getStatus(), is(equalTo(PASSED)));
    }

    @Test
    void result_is_skipped_when_skip_step_is_not_run_all() {
        ExecutionMode nextExecutionMode = step.run(testCase, bus, state, ExecutionMode.SKIP);
        assertThat(nextExecutionMode, is(ExecutionMode.SKIP));
        assertThat(state.getStatus(), is(equalTo(SKIPPED)));
    }

    @Test
    void result_is_skipped_when_before_step_hook_does_not_pass() {
        doThrow(TestAbortedException.class).when(beforeHookDefinition).execute(any(TestCaseState.class));
        ExecutionMode nextExecutionMode = step.run(testCase, bus, state, ExecutionMode.RUN);
        assertThat(nextExecutionMode, is(ExecutionMode.SKIP));
        assertThat(state.getStatus(), is(equalTo(SKIPPED)));
    }

    @Test
    void step_execution_is_skipped_when_before_step_hook_does_not_pass() throws Throwable {
        doThrow(TestAbortedException.class).when(beforeHookDefinition).execute(any(TestCaseState.class));
        step.run(testCase, bus, state, ExecutionMode.RUN);
        verify(definitionMatch, never()).runStep(any(TestCaseState.class));
        verify(definitionMatch, never()).dryRunStep(any(TestCaseState.class));
    }

    @Test
    void result_is_result_from_hook_when_before_step_hook_does_not_pass() {
        Exception exception = new RuntimeException();
        doThrow(exception).when(beforeHookDefinition).execute(any(TestCaseState.class));
        Result failure = new Result(Status.FAILED, ZERO, exception);
        ExecutionMode nextExecutionMode = step.run(testCase, bus, state, ExecutionMode.RUN);
        assertThat(nextExecutionMode, is(ExecutionMode.SKIP));
        assertThat(state.getStatus(), is(equalTo(FAILED)));

        ArgumentCaptor<TestCaseEvent> captor = forClass(TestCaseEvent.class);
        verify(bus, times(12)).send(captor.capture());
        List<TestCaseEvent> allValues = captor.getAllValues();
        assertThat(((TestStepFinished) allValues.get(2)).getResult(), is(equalTo(failure)));
    }

    @Test
    void result_is_result_from_step_when_step_hook_does_not_pass() throws Throwable {
        RuntimeException runtimeException = new RuntimeException();
        Result failure = new Result(Status.FAILED, ZERO, runtimeException);
        doThrow(runtimeException).when(definitionMatch).runStep(any(TestCaseState.class));
        ExecutionMode nextExecutionMode = step.run(testCase, bus, state, ExecutionMode.RUN);
        assertThat(nextExecutionMode, is(ExecutionMode.SKIP));
        assertThat(state.getStatus(), is(equalTo(FAILED)));

        ArgumentCaptor<TestCaseEvent> captor = forClass(TestCaseEvent.class);
        verify(bus, times(12)).send(captor.capture());
        List<TestCaseEvent> allValues = captor.getAllValues();
        assertThat(((TestStepFinished) allValues.get(6)).getResult(), is(equalTo(failure)));
    }

    @Test
    void result_is_result_from_hook_when_after_step_hook_does_not_pass() {
        Exception exception = new RuntimeException();
        Result failure = new Result(Status.FAILED, ZERO, exception);
        doThrow(exception).when(afterHookDefinition).execute(any(TestCaseState.class));
        ExecutionMode nextExecutionMode = step.run(testCase, bus, state, ExecutionMode.RUN);
        assertThat(nextExecutionMode, is(ExecutionMode.SKIP));
        assertThat(state.getStatus(), is(equalTo(FAILED)));

        ArgumentCaptor<Object> captor = forClass(TestCaseEvent.class);
        verify(bus, times(12)).send(captor.capture());
        List<Object> allValues = captor.getAllValues();
        assertThat(((TestStepFinished) allValues.get(10)).getResult(), is(equalTo(failure)));
    }

    @Test
    void after_step_hook_is_run_when_before_step_hook_does_not_pass() {
        doThrow(RuntimeException.class).when(beforeHookDefinition).execute(any(TestCaseState.class));
        step.run(testCase, bus, state, ExecutionMode.RUN);
        verify(afterHookDefinition).execute(any(TestCaseState.class));
    }

    @Test
    void after_step_hook_is_run_when_step_does_not_pass() throws Throwable {
        doThrow(Exception.class).when(definitionMatch).runStep(any(TestCaseState.class));
        step.run(testCase, bus, state, ExecutionMode.RUN);
        verify(afterHookDefinition).execute(any(TestCaseState.class));
    }

    @Test
    void after_step_hook_scenario_contains_step_failure_when_step_does_not_pass() throws Throwable {
        Throwable expectedError = new TestAbortedException("oops");
        doThrow(expectedError).when(definitionMatch).runStep(any(TestCaseState.class));
        doThrow(new RuntimeException()).when(afterHookDefinition).execute(argThat(scenarioDoesNotHave(expectedError)));
        step.run(testCase, bus, state, ExecutionMode.RUN);
        assertThat(state.getError(), is(expectedError));
    }

    private static ArgumentMatcher<TestCaseState> scenarioDoesNotHave(final Throwable type) {
        return argument -> !type.equals(argument.getError());
    }

    @Test
    void after_step_hook_scenario_contains_before_step_hook_failure_when_before_step_hook_does_not_pass() {
        Throwable expectedError = new TestAbortedException("oops");
        doThrow(expectedError).when(beforeHookDefinition).execute(any(TestCaseState.class));
        doThrow(new RuntimeException()).when(afterHookDefinition).execute(argThat(scenarioDoesNotHave(expectedError)));
        step.run(testCase, bus, state, ExecutionMode.RUN);
        assertThat(state.getError(), is(expectedError));
    }

    @Test
    void result_is_skipped_when_step_definition_throws_assumption_violated_exception() throws Throwable {
        doThrow(TestAbortedException.class).when(definitionMatch).runStep(any());

        ExecutionMode nextExecutionMode = step.run(testCase, bus, state, ExecutionMode.RUN);
        assertThat(nextExecutionMode, is(ExecutionMode.SKIP));

        assertThat(state.getStatus(), is(equalTo(SKIPPED)));
    }

    @Test
    void result_is_failed_when_step_definition_throws_exception() throws Throwable {
        doThrow(RuntimeException.class).when(definitionMatch).runStep(any(TestCaseState.class));

        ExecutionMode nextExecutionMode = step.run(testCase, bus, state, ExecutionMode.RUN);
        assertThat(nextExecutionMode, is(ExecutionMode.SKIP));

        assertThat(state.getStatus(), is(equalTo(FAILED)));
    }

    @Test
    void result_is_pending_when_step_definition_throws_pending_exception() throws Throwable {
        doThrow(StubPendingException.class).when(definitionMatch).runStep(any(TestCaseState.class));

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
        when(bus.getInstant()).thenReturn(ofEpochMilli(234L), ofEpochMilli(1234L));
        step.run(testCase, bus, state, ExecutionMode.RUN);

        ArgumentCaptor<TestCaseEvent> captor = forClass(TestCaseEvent.class);
        verify(bus, times(4)).send(captor.capture());

        List<TestCaseEvent> allValues = captor.getAllValues();
        TestStepStarted started = (TestStepStarted) allValues.get(0);
        TestStepFinished finished = (TestStepFinished) allValues.get(2);

        assertAll(
            () -> assertThat(started.getInstant(), is(equalTo(ofEpochMilli(234L)))),
            () -> assertThat(finished.getInstant(), is(equalTo(ofEpochMilli(1234L)))),
            () -> assertThat(finished.getResult().getDuration(), is(equalTo(ofMillis(1000L)))));
    }

}
