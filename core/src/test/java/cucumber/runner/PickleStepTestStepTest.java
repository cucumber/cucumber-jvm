package cucumber.runner;

import cucumber.api.PendingException;
import cucumber.api.Result;
import cucumber.api.Scenario;
import cucumber.api.event.TestStepFinished;
import cucumber.api.event.TestStepStarted;
import cucumber.runtime.HookDefinition;
import gherkin.events.PickleEvent;
import gherkin.pickles.PickleStep;
import org.junit.AssumptionViolatedException;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.Collections;

import static cucumber.api.HookType.AfterStep;
import static cucumber.api.HookType.BeforeStep;
import static cucumber.api.Result.Type.FAILED;
import static cucumber.api.Result.Type.PASSED;
import static cucumber.api.Result.Type.SKIPPED;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PickleStepTestStepTest {
    private PickleEvent pickle = mock(PickleEvent.class);
    private final TestCase testCase = new TestCase(Collections.<PickleStepTestStep>emptyList(), Collections.<HookTestStep>emptyList(), Collections.<HookTestStep>emptyList(), pickle, false);
    private final EventBus bus = mock(EventBus.class);
    private final Scenario scenario = mock(Scenario.class);
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
        Result result = step.run(testCase, bus, scenario, false);

        assertEquals(PASSED, result.getStatus());
    }

    @Test
    public void result_is_skipped_when_skip_step_is_not_run_all() {
        Result result = step.run(testCase, bus, scenario, true);

        assertEquals(SKIPPED, result.getStatus());
    }


    @Test
    public void result_is_skipped_when_before_step_hook_does_not_pass() throws Throwable {
        doThrow(AssumptionViolatedException.class).when(beforeHookDefinition).execute(any(Scenario.class));
        Result result = step.run(testCase, bus, scenario, false);
        assertEquals(SKIPPED, result.getStatus());
    }

    @Test
    public void step_execution_is_dry_run_when_before_step_hook_does_not_pass() throws Throwable {
        doThrow(AssumptionViolatedException.class).when(beforeHookDefinition).execute(any(Scenario.class));
        step.run(testCase, bus, scenario, false);
        verify(definitionMatch).dryRunStep(any(Scenario.class));
    }

    @Test
    public void result_is_result_from_hook_when_before_step_hook_does_not_pass() throws Throwable {
        Exception exception = new RuntimeException();
        doThrow(exception).when(beforeHookDefinition).execute(any(Scenario.class));
        Result result = step.run(testCase, bus, scenario, false);
        assertEquals(new Result(FAILED, 0L, exception), result);
    }

    @Test
    public void result_is_result_from_hook_when_after_step_hook_does_not_pass() throws Throwable {
        Exception exception = new RuntimeException();
        doThrow(exception).when(afterHookDefinition).execute(any(Scenario.class));
        Result result = step.run(testCase, bus, scenario, false);
        assertEquals(new Result(FAILED, 0L, exception), result);
    }


    @Test
    public void after_step_hook_is_run_when_before_step_hook_does_not_pass() throws Throwable {
        doThrow(RuntimeException.class).when(beforeHookDefinition).execute(any(Scenario.class));
        step.run(testCase, bus, scenario, false);
        verify(afterHookDefinition).execute(any(Scenario.class));
    }


    @Test
    public void after_step_hook_is_run_when_step_does_not_pass() throws Throwable {
        doThrow(Exception.class).when(definitionMatch).runStep((Scenario) any());
        step.run(testCase, bus, scenario, false);
        verify(afterHookDefinition).execute(any(Scenario.class));
    }


    @Test
    public void result_is_skipped_when_step_definition_throws_assumption_violated_exception() throws Throwable {
        doThrow(AssumptionViolatedException.class).when(definitionMatch).runStep((Scenario) any());

        Result result = step.run(testCase, bus, scenario, false);

        assertEquals(SKIPPED, result.getStatus());
    }

    @Test
    public void result_is_failed_when_step_definition_throws_exception() throws Throwable {
        doThrow(RuntimeException.class).when(definitionMatch).runStep((Scenario) any());

        Result result = step.run(testCase, bus, scenario, false);

        assertEquals(FAILED, result.getStatus());
    }

    @Test
    public void result_is_pending_when_step_definition_throws_pending_exception() throws Throwable {
        doThrow(PendingException.class).when(definitionMatch).runStep((Scenario) any());

        Result result = step.run(testCase, bus, scenario, false);

        assertEquals(Result.Type.PENDING, result.getStatus());
    }

    @Test
    public void step_execution_time_is_measured() {
        Long duration = 1234L;
        TestStep step = new PickleStepTestStep("uri", mock(PickleStep.class), definitionMatch);
        when(bus.getTime()).thenReturn(0L, 1234L);

        when(bus.getTime())
            .thenReturn(0L)
            .thenReturn(1234L);

        Result result = step.run(testCase, bus, scenario, false);

        assertEquals(duration, result.getDuration());
    }

}
