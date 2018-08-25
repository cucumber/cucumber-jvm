package cucumber.runner;

import cucumber.api.PickleStepTestStep;
import cucumber.api.Result;
import cucumber.api.TestCase;
import cucumber.api.event.EventHandler;
import cucumber.api.event.TestStepFinished;
import cucumber.api.event.TestStepStarted;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class EventBusTest {

    @Test
    public void handlers_receive_the_events_they_registered_for() {
        EventHandler<TestStepFinished> handler = mock(EventHandler.class);
        PickleStepTestStep testStep = mock(PickleStepTestStep.class);
        Result result = new Result(Result.Type.PASSED, 0L, null);
        TestCase testCase = mock(TestCase.class);
        TestStepFinished event = new TestStepFinished(0L, testCase, testStep, result);

        EventBus bus = new TimeServiceEventBus(new TimeServiceStub(0));
        bus.registerHandlerFor(TestStepFinished.class, handler);
        bus.send(event);

        verify(handler).receive(event);
    }

    @Test
    public void handlers_do_not_receive_the_events_they_did_not_registered_for() {
        EventHandler handler = mock(EventHandler.class);
        PickleStepTestStep testStep = mock(PickleStepTestStep.class);
        TestCase testCase = mock(TestCase.class);
        TestStepStarted event = new TestStepStarted(0L, testCase, testStep);

        EventBus bus = new TimeServiceEventBus(new TimeServiceStub(0));
        bus.registerHandlerFor(TestStepFinished.class, handler);
        bus.send(event);

        verify(handler, never()).receive(event);
    }

}
