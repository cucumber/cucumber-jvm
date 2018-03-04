package cucumber.runner;

import cucumber.api.HookType;
import cucumber.api.Result;
import cucumber.api.Scenario;
import cucumber.api.TestCase;
import cucumber.api.event.TestStepFinished;
import cucumber.api.event.TestStepStarted;
import cucumber.runtime.DefinitionMatch;
import org.junit.Test;
import org.mockito.InOrder;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

public class UnskipableHookStepTest {
    private final DefinitionMatch definitionMatch = mock(DefinitionMatch.class);
    private final EventBus bus = mock(EventBus.class);
    private final String language = "en";
    private final Scenario scenario = mock(Scenario.class);

    @Test
    public void run_does_run_step_even_when_skip_steps_is_skip_all_skippable() throws Throwable {

        HookStep step = new UnskipableHookStep(HookType.Before, definitionMatch);
        step.run(bus, language, scenario, TestCase.SkipStatus.SKIP_ALL_SKIPABLE);

        InOrder order = inOrder(bus, definitionMatch);
        order.verify(bus).send(isA(TestStepStarted.class));
        order.verify(definitionMatch).runStep(language, scenario);
        order.verify(bus).send(isA(TestStepFinished.class));
    }

    @Test
    public void result_is_passed_when_step_definition_does_not_throw_exception_and_skip_steps_is_skip_all_skipable() throws Throwable {
        HookStep step = new UnskipableHookStep(HookType.Before, definitionMatch);
        Result result = step.run(bus, language, scenario, TestCase.SkipStatus.SKIP_ALL_SKIPABLE);

        assertEquals(Result.Type.PASSED, result.getStatus());
    }
}
