package io.cucumber.core.runner;

import io.cucumber.core.api.event.EventHandler;
import io.cucumber.core.api.event.EventListener;
import io.cucumber.core.api.event.EventPublisher;
import io.cucumber.core.api.event.TestStepStarted;

import java.time.Duration;
import java.time.Instant;

public class StepDurationTimeService implements TimeService, EventListener {

    private final ThreadLocal<Instant> currentInstant = new ThreadLocal<>();
    private final Duration stepDuration;

    private EventHandler<TestStepStarted> stepStartedHandler = new EventHandler<TestStepStarted>() {
        @Override
        public void receive(TestStepStarted event) {
            handleTestStepStarted();
        }
    };
    
    public StepDurationTimeService(Duration stepDuration) {
        this.stepDuration = stepDuration;
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestStepStarted.class, stepStartedHandler);
    }

    @Override
    public Instant timeInstant() {
        Instant result = currentInstant.get();
        return result != null ? result : Instant.EPOCH;
    }

    private void handleTestStepStarted() {
        Instant timeInstant = timeInstant();
        currentInstant.set(timeInstant.plus(stepDuration));
    }

}
