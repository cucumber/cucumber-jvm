package io.cucumber.core.runner;

import io.cucumber.core.event.Result;
import io.cucumber.core.event.Status;
import io.cucumber.core.event.TestCaseEvent;
import io.cucumber.core.event.TestStepFinished;
import io.cucumber.core.event.TestStepStarted;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.feature.CucumberFeature;
import io.cucumber.core.feature.CucumberPickle;
import io.cucumber.core.feature.TestFeatureParser;
import org.junit.AssumptionViolatedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static io.cucumber.core.event.HookType.AFTER_STEP;
import static io.cucumber.core.event.HookType.BEFORE_STEP;
import static io.cucumber.core.event.Status.FAILED;
import static io.cucumber.core.event.Status.PASSED;
import static io.cucumber.core.event.Status.SKIPPED;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PickleStepTestStepTest {

    private final CucumberFeature feature = TestFeatureParser.parse("" +
        "Feature: Test feature\n" +
        "  Scenario: Test scenario\n" +
        "     Given I have 4 cukes in my belly\n"
    );
    private final CucumberPickle pickle = feature.getPickles().get(0);
    private final TestCase testCase = new TestCase(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), pickle, false);
    private final EventBus bus = mock(EventBus.class);
    private final Scenario scenario = new Scenario(bus, testCase);
    private final PickleStepDefinitionMatch definitionMatch = mock(PickleStepDefinitionMatch.class);
    private CoreHookDefinition afterHookDefinition = mock(CoreHookDefinition.class);
    private CoreHookDefinition beforeHookDefinition = mock(CoreHookDefinition.class);
    private final HookTestStep beforeHook = new HookTestStep(BEFORE_STEP, new HookDefinitionMatch(beforeHookDefinition));
    private final HookTestStep afterHook = new HookTestStep(AFTER_STEP, new HookDefinitionMatch(afterHookDefinition));
    private final PickleStepTestStep step = new PickleStepTestStep(
        "uri",
        pickle.getSteps().get(0),
        singletonList(beforeHook),
        singletonList(afterHook),
        definitionMatch
    );

    @BeforeEach
    void init() {
        Mockito.when(bus.getInstant()).thenReturn(Instant.now());
    }

    @Test
    void run_wraps_run_step_in_test_step_started_and_finished_events() throws Throwable {
        step.run(testCase, bus, scenario, false);

        InOrder order = inOrder(bus, definitionMatch);
        order.verify(bus).send(isA(TestStepStarted.class));
        order.verify(definitionMatch).runStep(scenario);
        order.verify(bus).send(isA(TestStepFinished.class));
    }

    @Test
    void run_does_dry_run_step_when_skip_steps_is_true() throws Throwable {
        step.run(testCase, bus, scenario, true);

        InOrder order = inOrder(bus, definitionMatch);
        order.verify(bus).send(isA(TestStepStarted.class));
        order.verify(definitionMatch).dryRunStep(scenario);
        order.verify(bus).send(isA(TestStepFinished.class));
    }

    @Test
    void result_is_passed_when_step_definition_does_not_throw_exception() {
        boolean skipNextStep = step.run(testCase, bus, scenario, false);
        assertFalse(skipNextStep);
        assertThat(scenario.getStatus(), is(equalTo(PASSED)));
    }

    @Test
    void result_is_skipped_when_skip_step_is_not_run_all() {
        boolean skipNextStep = step.run(testCase, bus, scenario, true);

        assertTrue(skipNextStep);
        assertThat(scenario.getStatus(), is(equalTo(SKIPPED)));
    }

    @Test
    void result_is_skipped_when_before_step_hook_does_not_pass() throws Throwable {
        doThrow(AssumptionViolatedException.class).when(beforeHookDefinition).execute(any(io.cucumber.core.api.Scenario.class));
        boolean skipNextStep = step.run(testCase, bus, scenario, false);
        assertTrue(skipNextStep);
        assertThat(scenario.getStatus(), is(equalTo(SKIPPED)));
    }

    @Test
    void step_execution_is_dry_run_when_before_step_hook_does_not_pass() throws Throwable {
        doThrow(AssumptionViolatedException.class).when(beforeHookDefinition).execute(any(io.cucumber.core.api.Scenario.class));
        step.run(testCase, bus, scenario, false);
        verify(definitionMatch).dryRunStep(any(Scenario.class));
    }

    @Test
    void result_is_result_from_hook_when_before_step_hook_does_not_pass() throws Throwable {
        Exception exception = new RuntimeException();
        doThrow(exception).when(beforeHookDefinition).execute(any(io.cucumber.core.api.Scenario.class));
        Result failure = new Result(Status.FAILED, ZERO, exception);
        boolean skipNextStep = step.run(testCase, bus, scenario, false);
        assertTrue(skipNextStep);
        assertThat(scenario.getStatus(), is(equalTo(FAILED)));

        ArgumentCaptor<TestCaseEvent> captor = forClass(TestCaseEvent.class);
        verify(bus, times(6)).send(captor.capture());
        List<TestCaseEvent> allValues = captor.getAllValues();
        assertThat(((TestStepFinished) allValues.get(1)).getResult(), is(equalTo(failure)));
    }

    @Test
    void result_is_result_from_step_when_step_hook_does_not_pass() throws Throwable {
        RuntimeException runtimeException = new RuntimeException();
        Result failure = new Result(Status.FAILED, ZERO, runtimeException);
        doThrow(runtimeException).when(definitionMatch).runStep(any(Scenario.class));
        boolean skipNextStep = step.run(testCase, bus, scenario, false);
        assertTrue(skipNextStep);
        assertThat(scenario.getStatus(), is(equalTo(FAILED)));

        ArgumentCaptor<TestCaseEvent> captor = forClass(TestCaseEvent.class);
        verify(bus, times(6)).send(captor.capture());
        List<TestCaseEvent> allValues = captor.getAllValues();
        assertThat(((TestStepFinished) allValues.get(3)).getResult(), is(equalTo(failure)));
    }

    @Test
    void result_is_result_from_hook_when_after_step_hook_does_not_pass() throws Throwable {
        Exception exception = new RuntimeException();
        Result failure = new Result(Status.FAILED, ZERO, exception);
        doThrow(exception).when(afterHookDefinition).execute(any(io.cucumber.core.api.Scenario.class));
        boolean skipNextStep = step.run(testCase, bus, scenario, false);
        assertTrue(skipNextStep);
        assertThat(scenario.getStatus(), is(equalTo(FAILED)));

        ArgumentCaptor<TestCaseEvent> captor = forClass(TestCaseEvent.class);
        verify(bus, times(6)).send(captor.capture());
        List<TestCaseEvent> allValues = captor.getAllValues();
        assertThat(((TestStepFinished) allValues.get(5)).getResult(), is(equalTo(failure)));
    }

    @Test
    void after_step_hook_is_run_when_before_step_hook_does_not_pass() throws Throwable {
        doThrow(RuntimeException.class).when(beforeHookDefinition).execute(any(io.cucumber.core.api.Scenario.class));
        step.run(testCase, bus, scenario, false);
        verify(afterHookDefinition).execute(any(io.cucumber.core.api.Scenario.class));
    }

    @Test
    void after_step_hook_is_run_when_step_does_not_pass() throws Throwable {
        doThrow(Exception.class).when(definitionMatch).runStep(any(Scenario.class));
        step.run(testCase, bus, scenario, false);
        verify(afterHookDefinition).execute(any(io.cucumber.core.api.Scenario.class));
    }

    @Test
    void after_step_hook_scenario_contains_step_failure_when_step_does_not_pass() throws Throwable {
        Throwable expectedError = new AssumptionViolatedException("oops");
        doThrow(expectedError).when(definitionMatch).runStep(any(Scenario.class));
        doThrow(new Exception()).when(afterHookDefinition).execute(argThat(scenarioDoesNotHave(expectedError)));
        step.run(testCase, bus, scenario, false);
        assertThat(scenario.getError(), is(expectedError));
    }

    @Test
    void after_step_hook_scenario_contains_before_step_hook_failure_when_before_step_hook_does_not_pass() throws Throwable {
        Throwable expectedError = new AssumptionViolatedException("oops");
        doThrow(expectedError).when(beforeHookDefinition).execute(any(Scenario.class));
        doThrow(new Exception()).when(afterHookDefinition).execute(argThat(scenarioDoesNotHave(expectedError)));
        step.run(testCase, bus, scenario, false);
        assertThat(scenario.getError(), is(expectedError));
    }

    private static ArgumentMatcher<Scenario> scenarioDoesNotHave(final Throwable type) {
        return argument -> !type.equals(argument.getError());
    }

    @Test
    void result_is_skipped_when_step_definition_throws_assumption_violated_exception() throws Throwable {
        doThrow(AssumptionViolatedException.class).when(definitionMatch).runStep(any());

        boolean skipNextStep = step.run(testCase, bus, scenario, false);
        assertTrue(skipNextStep);

        assertThat(scenario.getStatus(), is(equalTo(SKIPPED)));
    }

    @Test
    void result_is_failed_when_step_definition_throws_exception() throws Throwable {
        doThrow(RuntimeException.class).when(definitionMatch).runStep(any(Scenario.class));

        boolean skipNextStep = step.run(testCase, bus, scenario, false);
        assertTrue(skipNextStep);

        assertThat(scenario.getStatus(), is(equalTo(Status.FAILED)));
    }

    @Test
    void result_is_pending_when_step_definition_throws_pending_exception() throws Throwable {
        doThrow(TestPendingException.class).when(definitionMatch).runStep(any(Scenario.class));

        boolean skipNextStep = step.run(testCase, bus, scenario, false);
        assertTrue(skipNextStep);

        assertThat(scenario.getStatus(), is(equalTo(Status.PENDING)));
    }

    @Test
    void step_execution_time_is_measured() {
        CucumberFeature feature = TestFeatureParser.parse("" +
            "Feature: Test feature\n" +
            "  Scenario: Test scenario\n" +
            "     Given I have 4 cukes in my belly\n"
        );

        TestStep step = new PickleStepTestStep(
            "file:path/to.feature",
            feature.getPickles().get(0).getSteps().get(0),
            definitionMatch
        );
        when(bus.getInstant()).thenReturn(ofEpochMilli(234L), ofEpochMilli(1234L));
        step.run(testCase, bus, scenario, false);

        ArgumentCaptor<TestCaseEvent> captor = forClass(TestCaseEvent.class);
        verify(bus, times(2)).send(captor.capture());

        List<TestCaseEvent> allValues = captor.getAllValues();
        TestStepStarted started = (TestStepStarted) allValues.get(0);
        TestStepFinished finished = (TestStepFinished) allValues.get(1);

        assertAll("Checking TestStep",
            () -> assertThat(started.getInstant(), is(equalTo(ofEpochMilli(234L)))),
            () -> assertThat(finished.getInstant(), is(equalTo(ofEpochMilli(1234L)))),
            () -> assertThat(finished.getResult().getDuration(), is(equalTo(ofMillis(1000L))))
        );
    }

}
