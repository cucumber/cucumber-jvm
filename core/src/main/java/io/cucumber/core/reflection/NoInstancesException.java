package io.cucumber.core.reflection;

import io.cucumber.core.exception.CucumberException;

public final class NoInstancesException extends CucumberException {

    NoInstancesException(Class parentType) {
        super(createMessage(parentType));
    }

    private static String createMessage(Class parentType) {
        return String.format("Couldn't find a single implementation of " + parentType);
    }
}
