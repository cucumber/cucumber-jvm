package io.cucumber.core.runner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Test;

import io.cucumber.core.api.event.EventHandler;
import io.cucumber.core.api.event.PickleStepTestStep;
import io.cucumber.core.api.event.Result;
import io.cucumber.core.api.event.TestCase;
import io.cucumber.core.api.event.TestStepFinished;
import io.cucumber.core.api.event.TestStepStarted;
import io.cucumber.core.event.EventBus;

public class EventBusTest {

    @Test
    public void handlers_receive_the_events_they_registered_for() {
        EventHandler<TestStepFinished> handler = mock(EventHandler.class);
        PickleStepTestStep testStep = mock(PickleStepTestStep.class);
        Result result = new Result(Result.Type.PASSED, 0L, null);
        TestCase testCase = mock(TestCase.class);
        TestStepFinished event = new TestStepFinished(0L, 0L, testCase, testStep, result);

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
        TestStepStarted event = new TestStepStarted(0L, 0L, testCase, testStep);

        EventBus bus = new TimeServiceEventBus(new TimeServiceStub(0));
        bus.registerHandlerFor(TestStepFinished.class, handler);
        bus.send(event);

        verify(handler, never()).receive(event);
    }

}
