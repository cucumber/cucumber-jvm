package cucumber.runner;

import cucumber.api.HookType;
import cucumber.api.Result;
import cucumber.api.event.TestStepFinished;
import cucumber.api.event.TestStepStarted;
import cucumber.runner.EventBus;
import cucumber.runtime.HookDefinition;
import gherkin.events.PickleEvent;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

public class HookTestStepTest {
    private final HookDefinition hookDefintion = mock(HookDefinition.class);
    private final HookDefinitionMatch definitionMatch = new HookDefinitionMatch(hookDefintion);
    private final TestCase testCase = new TestCase(
        Collections.<PickleStepTestStep>emptyList(),
        Collections.<HookTestStep>emptyList(),
        Collections.<HookTestStep>emptyList(),
        mock(PickleEvent.class),
        false
    );
    private final EventBus bus = mock(EventBus.class);
    private final Scenario scenario = new Scenario(bus, testCase);
    private HookTestStep step = new HookTestStep(HookType.AfterStep, definitionMatch);

    @Test
    public void run_does_run() throws Throwable {
        step.run(testCase, bus, scenario, false);

        InOrder order = inOrder(bus, hookDefintion);
        order.verify(bus).send(isA(TestStepStarted.class));
        order.verify(hookDefintion).execute(scenario);
        order.verify(bus).send(isA(TestStepFinished.class));
    }

    @Test
    public void run_does_dry_run() throws Throwable {
        step.run(testCase, bus, scenario, true);

        InOrder order = inOrder(bus, hookDefintion);
        order.verify(bus).send(isA(TestStepStarted.class));
        order.verify(hookDefintion, never()).execute(scenario);
        order.verify(bus).send(isA(TestStepFinished.class));
    }

    @Test
    public void result_is_passed_when_step_definition_does_not_throw_exception() {
        boolean skipNextStep = step.run(testCase, bus,  scenario, false);
        assertFalse(skipNextStep);
        assertEquals(Result.Type.PASSED, scenario.getStatus());
    }

    @Test
    public void result_is_skipped_when_skip_step_is_skip_all_skipable() {
        boolean skipNextStep = step.run(testCase, bus,  scenario, true);
        assertTrue(skipNextStep);
        assertEquals(Result.Type.SKIPPED, scenario.getStatus());
    }
}
