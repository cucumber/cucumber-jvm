package cucumber.api;

import cucumber.api.event.TestStepFinished;
import cucumber.api.event.TestStepStarted;
import cucumber.runner.EventBus;
import cucumber.runner.PickleTestStep;
import cucumber.runtime.DefinitionMatch;
import gherkin.pickles.PickleStep;
import org.junit.AssumptionViolatedException;
import org.junit.Test;
import org.mockito.InOrder;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestStepTest {
    private final EventBus bus = mock(EventBus.class);
    private final String language = "en";
    private final Scenario scenario = mock(Scenario.class);
    private final DefinitionMatch definitionMatch = mock(DefinitionMatch.class);
    private final TestStep step = new PickleTestStep("uri", mock(PickleStep.class), definitionMatch);

    @Test
    public void run_wraps_run_step_in_test_step_started_and_finished_events() throws Throwable {
        step.run(bus, language, scenario, false);

        InOrder order = inOrder(bus, definitionMatch);
        order.verify(bus).send(isA(TestStepStarted.class));
        order.verify(definitionMatch).runStep(language, scenario);
        order.verify(bus).send(isA(TestStepFinished.class));
    }

    @Test
    public void run_does_dry_run_step_when_skip_steps_is_true() throws Throwable {
        step.run(bus, language, scenario, true);

        InOrder order = inOrder(bus, definitionMatch);
        order.verify(bus).send(isA(TestStepStarted.class));
        order.verify(definitionMatch).dryRunStep(language, scenario);
        order.verify(bus).send(isA(TestStepFinished.class));
    }

    @Test
    public void result_is_passed_when_step_definition_does_not_throw_exception() throws Throwable {
        Result result = step.run(bus, language, scenario, false);

        assertEquals(Result.Type.PASSED, result.getStatus());
    }

    @Test
    public void result_is_skipped_when_skip_step_is_true() throws Throwable {
        Result result = step.run(bus, language, scenario, true);

        assertEquals(Result.Type.SKIPPED, result.getStatus());
    }

    @Test
    public void result_is_skipped_when_step_definition_throws_assumption_violated_exception() throws Throwable {
        doThrow(AssumptionViolatedException.class).when(definitionMatch).runStep(anyString(), (Scenario)any());

        Result result = step.run(bus, language, scenario, false);

        assertEquals(Result.Type.SKIPPED, result.getStatus());
    }

    @Test
    public void result_is_failed_when_step_definition_throws_exception() throws Throwable {
        doThrow(RuntimeException.class).when(definitionMatch).runStep(anyString(), (Scenario)any());

        Result result = step.run(bus, language, scenario, false);

        assertEquals(Result.Type.FAILED, result.getStatus());
    }

    @Test
    public void result_is_pending_when_step_definition_throws_pending_exception() throws Throwable {
        doThrow(PendingException.class).when(definitionMatch).runStep(anyString(), (Scenario)any());

        Result result = step.run(bus, language, scenario, false);

        assertEquals(Result.Type.PENDING, result.getStatus());
    }

    @Test
    public void step_execution_time_is_measured() throws Throwable {
        Long duration = 1234L;
        TestStep step = new PickleTestStep("uri", mock(PickleStep.class), definitionMatch);
        when(bus.getTime()).thenReturn(0l, 1234l);

        when(bus.getTime())
            .thenReturn(0L)
            .thenReturn(1234L);

        Result result = step.run(bus, language, scenario, false);

        assertEquals(duration, result.getDuration());
    }

}
