package cucumber.api.event;

import cucumber.runtime.StepDefinition;

public class StepDefinedEvent extends TimeStampedEvent {
    public final StepDefinition stepDefinition;

    public StepDefinedEvent(Long time, Long timeMillis, StepDefinition stepDefinition) {
        super(time, timeMillis);
        this.stepDefinition = stepDefinition;
    }

}
