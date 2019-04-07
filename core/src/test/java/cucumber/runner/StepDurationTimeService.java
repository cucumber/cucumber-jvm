package cucumber.runner;

import cucumber.api.event.EventHandler;
import cucumber.api.event.EventListener;
import cucumber.api.event.EventPublisher;
import cucumber.api.event.TestStepStarted;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class StepDurationTimeService implements TimeService, EventListener {

    private final ThreadLocal<Long> currentTime = new ThreadLocal<>();
    private final long stepDurationMillis;

    private EventHandler<TestStepStarted> stepStartedHandler = new EventHandler<TestStepStarted>() {
        @Override
        public void receive(TestStepStarted event) {
            handleTestStepStarted();
        }
    };

    public StepDurationTimeService(long stepDurationMillis) {
        this.stepDurationMillis = stepDurationMillis;
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestStepStarted.class, stepStartedHandler);
    }

    @Override
    public long time() {
        Long result = currentTime.get();
        return result != null ? MILLISECONDS.toNanos(result) : 0L;
    }

    @Override
    public long timeMillis() {
        Long result = currentTime.get();
        return result != null ? result : 0L;
    }

    private void handleTestStepStarted() {
        long time = timeMillis();
        currentTime.set(time + stepDurationMillis);
    }

}
