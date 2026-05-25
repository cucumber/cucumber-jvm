package io.cucumber.core.runner;

import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.exception.CucumberException;

import static java.util.Objects.requireNonNull;

public final class DuplicateStepDefinitionException extends CucumberException {

    private final StepDefinition stepDefinitionA;
    private final StepDefinition stepDefinitionB;

    public DuplicateStepDefinitionException(StepDefinition a, StepDefinition b) {
        super(createMessage(a, b));
        this.stepDefinitionA = requireNonNull(a);
        this.stepDefinitionB = requireNonNull(b);
    }

    public StepDefinition getStepDefinitionA() {
        return stepDefinitionA;
    }

    public StepDefinition getStepDefinitionB() {
        return stepDefinitionB;
    }

    private static String createMessage(StepDefinition a, StepDefinition b) {
        requireNonNull(a);
        requireNonNull(b);

        return "Duplicate step definitions in %s and %s".formatted(
            a.getLocation(),
            b.getLocation());
    }

}
