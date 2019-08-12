package io.cucumber.core.reflection;

import io.cucumber.core.exception.CucumberException;

import java.util.Collection;
import java.util.Objects;

final class TooManyInstancesException extends CucumberException {

    TooManyInstancesException(Collection<?> instances) {
        super(createMessage(instances));
    }

    private static String createMessage(Collection<?> instances) {
        Objects.requireNonNull(instances);
        return String.format("Expected only one instance, but found too many: %s", instances);
    }
}
