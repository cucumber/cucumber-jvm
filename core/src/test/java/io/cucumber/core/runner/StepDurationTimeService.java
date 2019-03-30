package io.cucumber.core.runner;

import io.cucumber.core.api.event.EventHandler;
import io.cucumber.core.api.event.EventListener;
import io.cucumber.core.api.event.EventPublisher;
import io.cucumber.core.api.event.TestStepStarted;

public class StepDurationTimeService implements TimeService, EventListener {
    private long stepDuration;
    private final ThreadLocal<Long> currentTime = new ThreadLocal<Long>();
    private EventHandler<TestStepStarted> stepStartedHandler = new EventHandler<TestStepStarted>() {
        @Override
        public void receive(TestStepStarted event) {
            handleTestStepStarted(event);
        }
    };


    public StepDurationTimeService(long stepDuration) {
        this.stepDuration = stepDuration;
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestStepStarted.class, stepStartedHandler);
    }

    @Override
    public long time() {
        Long result = currentTime.get();
        return result != null ? result : 0L;
    }
    
    @Override
    public long timeStampMillis() {
        Long result = currentTime.get();
        return result != null ? result : 0L;
    }

    private void handleTestStepStarted(TestStepStarted event) {
        long time = time();
        currentTime.set(time + stepDuration);
    }

}
