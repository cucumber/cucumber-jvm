package io.cucumber.core.exception;

import java.util.List;

public final class CompositeCucumberException extends CucumberException {

    public CompositeCucumberException(List<Throwable> causes) {
        super("There were %d exceptions. The details are in the stacktrace below.".formatted(causes.size()));
        causes.forEach(this::addSuppressed);
    }

}
