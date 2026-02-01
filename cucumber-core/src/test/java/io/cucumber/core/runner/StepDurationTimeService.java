package io.cucumber.core.runner;

import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventHandler;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.TestStepStarted;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("NullAway")
public final class StepDurationTimeService extends Clock implements ConcurrentEventListener {

    @SuppressWarnings("ThreadLocalUsage")
    private final ThreadLocal<Instant> currentInstant = new ThreadLocal<>();
    private final List<Duration> stepDuration;
    private int currentStepDurationIndex;

    private final EventHandler<TestStepStarted> stepStartedHandler = event -> handleTestStepStarted();

    public StepDurationTimeService(Duration... stepDuration) {
        this.stepDuration = Arrays.asList(stepDuration);
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestStepStarted.class, stepStartedHandler);
    }

    private void handleTestStepStarted() {
        Instant timeInstant = instant();
        currentInstant.set(timeInstant.plus(stepDuration.get(currentStepDurationIndex)));
        currentStepDurationIndex = (currentStepDurationIndex + 1) % stepDuration.size();
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
