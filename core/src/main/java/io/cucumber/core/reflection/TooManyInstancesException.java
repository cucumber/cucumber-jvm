package io.cucumber.core.reflection;

import io.cucumber.core.exception.CucumberException;

import java.util.Collection;

public final class TooManyInstancesException extends CucumberException {

    TooManyInstancesException(Collection instances) {
        super(createMessage(instances));
    }

    private static String createMessage(Collection instances) {
        return "Expected only one instance, but found too many: " + instances;
    }
}
