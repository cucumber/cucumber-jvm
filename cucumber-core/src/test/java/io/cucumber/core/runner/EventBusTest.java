package io.cucumber.core.runner;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.plugin.StubPickleStepTestStep;
import io.cucumber.core.plugin.StubTestCase;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.plugin.event.EventHandler;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestStepFinished;
import io.cucumber.plugin.event.TestStepStarted;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.time.Duration.ZERO;
import static java.time.Instant.EPOCH;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EventBusTest {

    @Test
    void handlers_receive_the_events_they_registered_for() {
        MockEventHandler<TestStepFinished> handler = new MockEventHandler<>();
        PickleStepTestStep testStep = new StubPickleStepTestStep();
        Result result = new Result(Status.PASSED, ZERO, null);
        TestCase testCase = new StubTestCase();
        TestStepFinished event = new TestStepFinished(EPOCH, testCase, testStep, result);

        EventBus bus = new TimeServiceEventBus(Clock.fixed(Instant.EPOCH, ZoneId.of("UTC")), UUID::randomUUID);
        bus.registerHandlerFor(TestStepFinished.class, handler);
        bus.send(event);

        assertEquals(event, handler.events.get(0));
    }

    @Test
    void handlers_do_not_receive_the_events_they_did_not_registered_for() {
        MockEventHandler<TestStepFinished> handler = new MockEventHandler<>();
        PickleStepTestStep testStep = new StubPickleStepTestStep();
        TestCase testCase = new StubTestCase();
        TestStepStarted event = new TestStepStarted(EPOCH, testCase, testStep);

        EventBus bus = new TimeServiceEventBus(Clock.fixed(Instant.EPOCH, ZoneId.of("UTC")), UUID::randomUUID);
        bus.registerHandlerFor(TestStepFinished.class, handler);
        bus.send(event);

        assertEquals(0, handler.events.size());
    }

    private static class MockEventHandler<T> implements EventHandler<T> {
        final List<T> events = new ArrayList<>();
        @Override
        public void receive(T event) {
            events.add(event);
        }
    }
}
