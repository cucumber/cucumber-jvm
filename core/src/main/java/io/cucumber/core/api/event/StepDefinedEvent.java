package io.cucumber.core.api.event;


import io.cucumber.core.backend.StepDefinition;

import java.time.Instant;

public class StepDefinedEvent extends TimeStampedEvent {
    public final StepDefinition stepDefinition;

    public StepDefinedEvent(Instant timeInstant, StepDefinition stepDefinition) {
        super(timeInstant);
        this.stepDefinition = stepDefinition;
    }
}
