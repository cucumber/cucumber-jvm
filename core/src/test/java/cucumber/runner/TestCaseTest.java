package cucumber.runner;

import cucumber.api.Scenario;
import cucumber.api.event.TestCaseFinished;
import cucumber.api.event.TestCaseStarted;
import cucumber.runtime.HookDefinition;
import gherkin.events.PickleEvent;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleStep;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.Arrays;
import java.util.Collections;

import static cucumber.api.HookType.AfterStep;
import static cucumber.api.HookType.BeforeStep;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

public class TestCaseTest {

    private final EventBus bus = mock(EventBus.class);


    private final PickleStepDefinitionMatch definitionMatch1 = mock(PickleStepDefinitionMatch.class);
    private HookDefinition beforeStep1HookDefinition1 = mock(HookDefinition.class);
    private HookDefinition afterStep1HookDefinition1 = mock(HookDefinition.class);

    private final PickleStepTestStep testStep1 = new PickleStepTestStep(
        "uri",
        mock(PickleStep.class),
        singletonList(new HookTestStep(BeforeStep, new HookDefinitionMatch(beforeStep1HookDefinition1))),
        singletonList(new HookTestStep(AfterStep, new HookDefinitionMatch(afterStep1HookDefinition1))),
        definitionMatch1
    );

    private final PickleStepDefinitionMatch definitionMatch2 = mock(PickleStepDefinitionMatch.class);
    private HookDefinition beforeStep1HookDefinition2 = mock(HookDefinition.class);
    private HookDefinition afterStep1HookDefinition2 = mock(HookDefinition.class);

    private final PickleStepTestStep testStep2 = new PickleStepTestStep(
        "uri",
        mock(PickleStep.class),
        singletonList(new HookTestStep(BeforeStep, new HookDefinitionMatch(beforeStep1HookDefinition2))),
        singletonList(new HookTestStep(AfterStep, new HookDefinitionMatch(afterStep1HookDefinition2))),
        definitionMatch2
    );


    @Test
    public void run_wraps_execute_in_test_case_started_and_finished_events() throws Throwable {
        doThrow(new UndefinedStepDefinitionException()).when(definitionMatch1).runStep(isA(Scenario.class));

        createTestCase(testStep1).run(bus);

        InOrder order = inOrder(bus, definitionMatch1);
        order.verify(bus).send(isA(TestCaseStarted.class));
        order.verify(definitionMatch1).runStep(isA(Scenario.class));
        order.verify(bus).send(isA(TestCaseFinished.class));
    }

    @Test
    public void run_all_steps() throws Throwable {
        TestCase testCase = createTestCase(testStep1, testStep2);
        testCase.run(bus);

        InOrder order = inOrder(definitionMatch1, definitionMatch2);
        order.verify(definitionMatch1).runStep(isA(Scenario.class));
        order.verify(definitionMatch2).runStep(isA(Scenario.class));
    }

    @Test
    public void run_hooks_after_the_first_non_passed_result_for_gherkin_step() throws Throwable {
        doThrow(new UndefinedStepDefinitionException()).when(definitionMatch1).runStep(isA(Scenario.class));

        TestCase testCase = createTestCase(testStep1, testStep2);
        testCase.run(bus);

        InOrder order = inOrder(beforeStep1HookDefinition1, definitionMatch1, afterStep1HookDefinition1);
        order.verify(beforeStep1HookDefinition1).execute(isA(Scenario.class));
        order.verify(definitionMatch1).runStep(isA(Scenario.class));
        order.verify(afterStep1HookDefinition1).execute(isA(Scenario.class));
    }


    @Test
    public void skip_hooks_of_step_after_skipped_step() throws Throwable {
        doThrow(new UndefinedStepDefinitionException()).when(definitionMatch1).runStep(isA(Scenario.class));

        TestCase testCase = createTestCase(testStep1, testStep2);
        testCase.run(bus);

        InOrder order = inOrder(beforeStep1HookDefinition2, definitionMatch2, afterStep1HookDefinition2);
        order.verify(beforeStep1HookDefinition2, never()).execute(isA(Scenario.class));
        order.verify(definitionMatch2).dryRunStep(isA(Scenario.class));
        order.verify(afterStep1HookDefinition2, never()).execute(isA(Scenario.class));
    }

    @Test
    public void skip_steps_at_first_gherkin_step_after_non_passed_result() throws Throwable {
        doThrow(new UndefinedStepDefinitionException()).when(definitionMatch1).runStep(isA(Scenario.class));

        TestCase testCase = createTestCase(testStep1, testStep2);
        testCase.run(bus);

        InOrder order = inOrder(definitionMatch1, definitionMatch2);
        order.verify(definitionMatch1).runStep(isA(Scenario.class));
        order.verify(definitionMatch2).dryRunStep(isA(Scenario.class));
    }

    private TestCase createTestCase(PickleStepTestStep... steps) {
        return new TestCase(asList(steps), Collections.<HookTestStep>emptyList(), Collections.<HookTestStep>emptyList(), pickleEvent(), false);
    }

    private PickleEvent pickleEvent() {
        Pickle pickle = mock(Pickle.class);
        when(pickle.getLocations()).thenReturn(singletonList(new PickleLocation(1, 1)));
        return new PickleEvent("uri", pickle);
    }

}
