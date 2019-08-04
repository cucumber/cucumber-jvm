package io.cucumber.core.reflection;

import io.cucumber.core.exception.CucumberException;

import java.util.Collection;

public final class TooManyInstancesException
    extends CucumberException {

    TooManyInstancesException(final Collection instances) {
        super(createMessage(instances));
    }

    private static String createMessage(final Collection instances) {
        return String.format("Expected only one instance, but found too many: %s", instances);
    }

}
