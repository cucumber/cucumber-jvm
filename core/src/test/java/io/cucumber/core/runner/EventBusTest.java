package io.cucumber.core.runner;

import io.cucumber.core.event.EventHandler;
import io.cucumber.core.event.PickleStepTestStep;
import io.cucumber.core.event.Result;
import io.cucumber.core.event.Status;
import io.cucumber.core.event.TestCase;
import io.cucumber.core.event.TestStepFinished;
import io.cucumber.core.event.TestStepStarted;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.runtime.TimeServiceEventBus;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static java.time.Duration.ZERO;
import static java.time.Instant.EPOCH;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class EventBusTest {

    @Test
    public void handlers_receive_the_events_they_registered_for() {
        EventHandler<TestStepFinished> handler = mock(EventHandler.class);
        PickleStepTestStep testStep = mock(PickleStepTestStep.class);
        Result result = new Result(Status.PASSED, ZERO, null);
        TestCase testCase = mock(TestCase.class);
        TestStepFinished event = new TestStepFinished(EPOCH, testCase, testStep, result);

        EventBus bus = new TimeServiceEventBus(Clock.fixed(Instant.EPOCH, ZoneId.of("UTC")));
        bus.registerHandlerFor(TestStepFinished.class, handler);
        bus.send(event);

        verify(handler).receive(event);
    }

    @Test
    public void handlers_do_not_receive_the_events_they_did_not_registered_for() {
        EventHandler handler = mock(EventHandler.class);
        PickleStepTestStep testStep = mock(PickleStepTestStep.class);
        TestCase testCase = mock(TestCase.class);
        TestStepStarted event = new TestStepStarted(EPOCH, testCase, testStep);

        EventBus bus = new TimeServiceEventBus(Clock.fixed(Instant.EPOCH, ZoneId.of("UTC")));
        bus.registerHandlerFor(TestStepFinished.class, handler);
        bus.send(event);

        verify(handler, never()).receive(event);
    }

}
