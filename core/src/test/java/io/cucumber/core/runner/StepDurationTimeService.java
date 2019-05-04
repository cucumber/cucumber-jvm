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
//    private final ThreadLocal<Long> currentTime = new ThreadLocal<>();
//    private final long stepDurationMillis;

    private EventHandler<TestStepStarted> stepStartedHandler = new EventHandler<TestStepStarted>() {
        @Override
        public void receive(TestStepStarted event) {
            handleTestStepStarted();
        }
    };

//    public StepDurationTimeService(long stepDurationMillis) {
//        this.stepDurationMillis = stepDurationMillis;
//    }
    
    public StepDurationTimeService(Duration stepDuration) {
        this.stepDuration = stepDuration;
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestStepStarted.class, stepStartedHandler);
    }

    //gazler
//    @Override
//    public long time() {
//        Long result = currentTime.get();
//        return result != null ? MILLISECONDS.toNanos(result) : 0L;
//    }
//
//    @Override
//    public long timeMillis() {
//        Long result = currentTime.get();
//        return result != null ? result : 0L;
//    }
    
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
