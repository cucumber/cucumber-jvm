package cucumber.runner;

import cucumber.api.HookType;
import cucumber.api.Result;
import cucumber.api.Scenario;
import cucumber.api.event.TestStepFinished;
import cucumber.api.event.TestStepStarted;
import cucumber.runtime.HookDefinitionMatch;
import org.junit.Test;
import org.mockito.InOrder;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

public class HookTestStepTest {
    private final HookDefinitionMatch definitionMatch = mock(HookDefinitionMatch.class);
    private final EventBus bus = mock(EventBus.class);
    private final String language = "en";
    private final Scenario scenario = mock(Scenario.class);
    private HookTestStep step = new HookTestStep(HookType.AfterStep, definitionMatch);

    @Test
    public void run_does_run() throws Throwable {
        step.run(bus, language, scenario, false);

        InOrder order = inOrder(bus, definitionMatch);
        order.verify(bus).send(isA(TestStepStarted.class));
        order.verify(definitionMatch).runStep(language, scenario);
        order.verify(bus).send(isA(TestStepFinished.class));
    }

    @Test
    public void run_does_dry_run() throws Throwable {
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
    public void result_is_skipped_when_skip_step_is_skip_all_skipable() throws Throwable {
        Result result = step.run(bus, language, scenario, true);

        assertEquals(Result.Type.SKIPPED, result.getStatus());
    }
}
