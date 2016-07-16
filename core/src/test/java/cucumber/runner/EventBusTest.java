package cucumber.runner;

import cucumber.api.Result;
import cucumber.api.TestStep;
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
        TestStep testStep = mock(TestStep.class);
        Result result = mock(Result.class);
        TestStepFinished event = new TestStepFinished(testStep, result);

        EventBus bus = new EventBus();
        bus.registerHandlerFor(TestStepFinished.class, handler);
        bus.send(event);

        verify(handler).receive(event);
    }

    @Test
    public void handlers_do_not_receive_the_events_they_did_not_registered_for() {
        EventHandler handler = mock(EventHandler.class);
        TestStep testStep = mock(TestStep.class);
        TestStepStarted event = new TestStepStarted(testStep);

        EventBus bus = new EventBus();
        bus.registerHandlerFor(TestStepFinished.class, handler);
        bus.send(event);

        verify(handler, never()).receive(event);
    }

}
