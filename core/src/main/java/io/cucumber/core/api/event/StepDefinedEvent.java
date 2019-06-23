package io.cucumber.core.api.event;


import java.time.Instant;

public class StepDefinedEvent extends TimeStampedEvent {
    public final StepDefinition stepDefinition;

    public StepDefinedEvent(Instant timeInstant, StepDefinition stepDefinition) {
        super(timeInstant);
        this.stepDefinition = stepDefinition;
    }
}
