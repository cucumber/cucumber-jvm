package io.cucumber.core.runner;

import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.exception.CucumberException;

import java.util.Objects;

final class DuplicateStepDefinitionException extends CucumberException {

    DuplicateStepDefinitionException(final StepDefinition a, final StepDefinition b) {
        super(createMessage(a, b));
    }

    private static String createMessage(final StepDefinition a, final StepDefinition b) {
        return String.format("Duplicate step definitions in %s and %s",
            Objects.isNull(a) ? "\"null step definition\"" : a.getLocation(true),
            Objects.isNull(a) ? "\"null step definition\"" : b.getLocation(true)
        );
    }

}
