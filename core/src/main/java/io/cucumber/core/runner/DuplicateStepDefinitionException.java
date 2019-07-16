package io.cucumber.core.runner;

import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.exception.CucumberException;

final class DuplicateStepDefinitionException extends CucumberException {
    DuplicateStepDefinitionException(StepDefinition a, StepDefinition b) {
        super(createMessage(a, b));
    }

    private static String createMessage(StepDefinition a, StepDefinition b) {
        return String.format("Duplicate step definitions in %s and %s", a.getLocation(true), b.getLocation(true));
    }
}
