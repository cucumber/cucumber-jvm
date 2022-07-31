package io.cucumber.core.exception;

import java.util.List;

public final class CompositeCucumberException extends CucumberException {

    public CompositeCucumberException(List<Throwable> causes) {
        super(String.format("There were %d exceptions. The details are in the stacktrace below.", causes.size()));
        causes.forEach(this::addSuppressed);
    }

}
