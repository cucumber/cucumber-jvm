package io.cucumber.core.runner;

import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.exception.CucumberException;

import static java.util.Objects.requireNonNull;

final class DuplicateStepDefinitionException extends CucumberException {

    DuplicateStepDefinitionException(StepDefinition a, StepDefinition b) {
        super(createMessage(a, b));
    }

    private static String createMessage(StepDefinition a, StepDefinition b) {
        requireNonNull(a);
        requireNonNull(b);

        return String.format("Duplicate step definitions in %s and %s",
            a.getLocation(),
            b.getLocation());
    }

}
