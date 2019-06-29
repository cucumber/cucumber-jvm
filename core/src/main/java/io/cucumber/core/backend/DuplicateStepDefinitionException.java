package io.cucumber.core.backend;

import io.cucumber.core.exception.CucumberException;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE)
public final class DuplicateStepDefinitionException extends CucumberException {
    public DuplicateStepDefinitionException(StepDefinition a, StepDefinition b) {
        super(createMessage(a, b));
    }

    private static String createMessage(StepDefinition a, StepDefinition b) {
        return String.format("Duplicate step definitions in %s and %s", a.getLocation(true), b.getLocation(true));
    }
}
