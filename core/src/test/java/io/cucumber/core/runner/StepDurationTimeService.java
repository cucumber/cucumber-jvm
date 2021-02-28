package io.cucumber.core.runner;

import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventHandler;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.TestStepStarted;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

public class StepDurationTimeService extends Clock implements ConcurrentEventListener {

    private final ThreadLocal<Instant> currentInstant = new ThreadLocal<>();
    private final Duration stepDuration;

    private final EventHandler<TestStepStarted> stepStartedHandler = event -> handleTestStepStarted();

    public StepDurationTimeService(Duration stepDuration) {
        this.stepDuration = stepDuration;
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestStepStarted.class, stepStartedHandler);
    }

    private void handleTestStepStarted() {
        Instant timeInstant = instant();
        currentInstant.set(timeInstant.plus(stepDuration));
    }

    @Override
    public ZoneId getZone() {
        return null;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return null;
    }

    @Override
    public Instant instant() {
        Instant result = currentInstant.get();
        return result != null ? result : Instant.EPOCH;
    }

}
