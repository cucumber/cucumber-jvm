package cucumber.runner;

import cucumber.api.event.EventHandler;
import cucumber.api.event.EventListener;
import cucumber.api.event.EventPublisher;
import cucumber.api.event.TestStepStarted;
import cucumber.runner.TimeService;

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
        return result != null ? result : 0l;
    }

    private void handleTestStepStarted(TestStepStarted event) {
        long time = time();
        currentTime.set(time + stepDuration);
    }
}
