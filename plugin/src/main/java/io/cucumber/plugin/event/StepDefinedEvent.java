package io.cucumber.plugin.event;

import org.apiguardian.api.API;

import java.time.Instant;
import java.util.Objects;

@API(status = API.Status.STABLE)
public final class StepDefinedEvent extends TimeStampedEvent {

    private final StepDefinition stepDefinition;

    public StepDefinedEvent(Instant timeInstant, StepDefinition stepDefinition) {
        super(timeInstant);
        this.stepDefinition = Objects.requireNonNull(stepDefinition);
    }

    public StepDefinition getStepDefinition() {
        return stepDefinition;
    }

}
