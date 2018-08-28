package cucumber.runner;

import cucumber.api.PendingException;
import cucumber.api.Result;
import cucumber.api.event.TestCaseEvent;
import cucumber.api.event.TestStepFinished;
import cucumber.api.event.TestStepStarted;
import cucumber.runtime.HookDefinition;
import gherkin.events.PickleEvent;
import gherkin.pickles.PickleStep;
import org.junit.AssumptionViolatedException;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.InOrder;

import java.util.Collections;
import java.util.List;

import static cucumber.api.HookType.AfterStep;
import static cucumber.api.HookType.BeforeStep;
import static cucumber.api.Result.Type.FAILED;
import static cucumber.api.Result.Type.PASSED;
import static cucumber.api.Result.Type.SKIPPED;
import static java.util.Collections.singletonList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
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

public class PickleStepTestStepTest {
    private PickleEvent pickle = mock(PickleEvent.class);
    private final TestCase testCase = new TestCase(Collections.<PickleStepTestStep>emptyList(), Collections.<HookTestStep>emptyList(), Collections.<HookTestStep>emptyList(), pickle, false);
    private final EventBus bus = mock(EventBus.class);
    private final Scenario scenario = new Scenario(bus, testCase);
    private final PickleStepDefinitionMatch definitionMatch = mock(PickleStepDefinitionMatch.class);
    private HookDefinition afterHookDefinition = mock(HookDefinition.class);
    private HookDefinition beforeHookDefinition = mock(HookDefinition.class);
    private final HookTestStep beforeHook = new HookTestStep(BeforeStep, new HookDefinitionMatch(beforeHookDefinition));
    private final HookTestStep afterHook = new HookTestStep(AfterStep, new HookDefinitionMatch(afterHookDefinition));
    private final PickleStepTestStep step = new PickleStepTestStep(
        "uri",
        mock(PickleStep.class),
        singletonList(beforeHook),
        singletonList(afterHook),
        definitionMatch
    );

    @Test
    public void run_wraps_run_step_in_test_step_started_and_finished_events() throws Throwable {
        step.run(testCase, bus, scenario, false);

        InOrder order = inOrder(bus, definitionMatch);
        order.verify(bus).send(isA(TestStepStarted.class));
        order.verify(definitionMatch).runStep(scenario);
        order.verify(bus).send(isA(TestStepFinished.class));
    }

    @Test
    public void run_does_dry_run_step_when_skip_steps_is_true() throws Throwable {
        step.run(testCase, bus, scenario, true);

        InOrder order = inOrder(bus, definitionMatch);
        order.verify(bus).send(isA(TestStepStarted.class));
        order.verify(definitionMatch).dryRunStep(scenario);
        order.verify(bus).send(isA(TestStepFinished.class));
    }

    @Test
    public void result_is_passed_when_step_definition_does_not_throw_exception() {
        boolean skipNextStep = step.run(testCase, bus, scenario, false);
        assertFalse(skipNextStep);
        assertEquals(PASSED, scenario.getStatus());
    }

    @Test
    public void result_is_skipped_when_skip_step_is_not_run_all() {
        boolean skipNextStep = step.run(testCase, bus, scenario, true);

        assertTrue(skipNextStep);
        assertEquals(SKIPPED, scenario.getStatus());
    }


    @Test
    public void result_is_skipped_when_before_step_hook_does_not_pass() throws Throwable {
        doThrow(AssumptionViolatedException.class).when(beforeHookDefinition).execute(any(cucumber.api.Scenario.class));
        boolean skipNextStep = step.run(testCase, bus, scenario, false);
        assertTrue(skipNextStep);
        assertEquals(SKIPPED, scenario.getStatus());
    }

    @Test
    public void step_execution_is_dry_run_when_before_step_hook_does_not_pass() throws Throwable {
        doThrow(AssumptionViolatedException.class).when(beforeHookDefinition).execute(any(cucumber.api.Scenario.class));
        step.run(testCase, bus, scenario, false);
        verify(definitionMatch).dryRunStep(any(Scenario.class));
    }

    @Test
    public void result_is_result_from_hook_when_before_step_hook_does_not_pass() throws Throwable {
        Exception exception = new RuntimeException();
        doThrow(exception).when(beforeHookDefinition).execute(any(cucumber.api.Scenario.class));
        Result failure = new Result(Result.Type.FAILED, 0L, exception);
        boolean skipNextStep = step.run(testCase, bus, scenario, false);
        assertTrue(skipNextStep);
        assertEquals(FAILED, scenario.getStatus());

        ArgumentCaptor<TestCaseEvent> captor = forClass(TestCaseEvent.class);
        verify(bus, times(6)).send(captor.capture());
        List<TestCaseEvent> allValues = captor.getAllValues();
        assertEquals(failure, ((TestStepFinished) allValues.get(1)).result);
    }

    @Test
    public void result_is_result_from_step_when_step_hook_does_not_pass() throws Throwable {
        RuntimeException runtimeException = new RuntimeException();
        Result failure = new Result(Result.Type.FAILED, 0L, runtimeException);
        doThrow(runtimeException).when(definitionMatch).runStep(any(Scenario.class));
        boolean skipNextStep = step.run(testCase, bus, scenario, false);
        assertTrue(skipNextStep);
        assertEquals(FAILED, scenario.getStatus());

        ArgumentCaptor<TestCaseEvent> captor = forClass(TestCaseEvent.class);
        verify(bus, times(6)).send(captor.capture());
        List<TestCaseEvent> allValues = captor.getAllValues();
        assertEquals(failure, ((TestStepFinished) allValues.get(3)).result);
    }
    @Test
    public void result_is_result_from_hook_when_after_step_hook_does_not_pass() throws Throwable {
        Exception exception = new RuntimeException();
        Result failure = new Result(Result.Type.FAILED, 0L, exception);
        doThrow(exception).when(afterHookDefinition).execute(any(cucumber.api.Scenario.class));
        boolean skipNextStep = step.run(testCase, bus, scenario, false);
        assertTrue(skipNextStep);
        assertEquals(FAILED, scenario.getStatus());

        ArgumentCaptor<TestCaseEvent> captor = forClass(TestCaseEvent.class);
        verify(bus, times(6)).send(captor.capture());
        List<TestCaseEvent> allValues = captor.getAllValues();
        assertEquals(failure, ((TestStepFinished) allValues.get(5)).result);
    }
    @Test
    public void after_step_hook_is_run_when_before_step_hook_does_not_pass() throws Throwable {
        doThrow(RuntimeException.class).when(beforeHookDefinition).execute(any(cucumber.api.Scenario.class));
        step.run(testCase, bus, scenario, false);
        verify(afterHookDefinition).execute(any(cucumber.api.Scenario.class));
    }

    @Test
    public void after_step_hook_is_run_when_step_does_not_pass() throws Throwable {
        doThrow(Exception.class).when(definitionMatch).runStep(any(Scenario.class));
        step.run(testCase, bus, scenario, false);
        verify(afterHookDefinition).execute(any(cucumber.api.Scenario.class));
    }

    @Test
    public void after_step_hook_scenario_contains_step_failure_when_step_does_not_pass() throws Throwable {
        Throwable expectedError = new AssumptionViolatedException("oops");
        doThrow(expectedError).when(definitionMatch).runStep(any(Scenario.class));
        doThrow(new Exception()).when(afterHookDefinition).execute(argThat(scenarioDoesNotHave(expectedError)));
        step.run(testCase, bus, scenario, false);
        assertThat(scenario.getError(),is(expectedError));
    }

    @Test
    public void after_step_hook_scenario_contains_before_step_hook_failure_when_before_step_hook_does_not_pass() throws Throwable {
        Throwable expectedError = new AssumptionViolatedException("oops");
        doThrow(expectedError).when(beforeHookDefinition).execute(any(Scenario.class));
        doThrow(new Exception()).when(afterHookDefinition).execute(argThat(scenarioDoesNotHave(expectedError)));
        step.run(testCase, bus, scenario, false);
        assertThat(scenario.getError(),is(expectedError));
    }

    private static ArgumentMatcher<Scenario> scenarioDoesNotHave(final Throwable type) {
        return new ArgumentMatcher<Scenario>() {
            @Override
            public boolean matches(Scenario argument) {
                return !type.equals(argument.getError());
            }
        };
    }

    @Test
    public void result_is_skipped_when_step_definition_throws_assumption_violated_exception() throws Throwable {
        doThrow(AssumptionViolatedException.class).when(definitionMatch).runStep((Scenario) any());

        boolean skipNextStep = step.run(testCase, bus, scenario, false);
        assertTrue(skipNextStep);

        assertEquals(SKIPPED, scenario.getStatus());
    }

    @Test
    public void result_is_failed_when_step_definition_throws_exception() throws Throwable {
        doThrow(RuntimeException.class).when(definitionMatch).runStep(any(Scenario.class));

        boolean skipNextStep = step.run(testCase, bus, scenario, false);
        assertTrue(skipNextStep);

        assertEquals(Result.Type.FAILED, scenario.getStatus());
    }

    @Test
    public void result_is_pending_when_step_definition_throws_pending_exception() throws Throwable {
        doThrow(PendingException.class).when(definitionMatch).runStep(any(Scenario.class));

        boolean skipNextStep = step.run(testCase, bus, scenario, false);
        assertTrue(skipNextStep);

        assertEquals(Result.Type.PENDING, scenario.getStatus());
    }

    @Test
    public void step_execution_time_is_measured() {
        TestStep step = new PickleStepTestStep("uri", mock(PickleStep.class), definitionMatch);
        when(bus.getTime()).thenReturn(234L, (Long) 1234L);
        step.run(testCase, bus, scenario, false);

        ArgumentCaptor<TestCaseEvent> captor = forClass(TestCaseEvent.class);
        verify(bus, times(2)).send(captor.capture());

        List<TestCaseEvent> allValues = captor.getAllValues();
        TestStepStarted started = (TestStepStarted) allValues.get(0);
        TestStepFinished finished = (TestStepFinished) allValues.get(1);

        assertEquals((Long) 234L, started.getTimeStamp());
        assertEquals((Long) 1234L, finished.getTimeStamp());
        assertEquals((Long) 1000L, finished.result.getDuration());
    }

}
