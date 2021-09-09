package io.cucumber.core.runner;

import io.cucumber.core.stepexpression.Argument;

import java.util.List;

import static java.util.Objects.requireNonNull;

abstract class Match {

    private final List<Argument> arguments;
    private final String location;

    Match(List<Argument> arguments, String location) {
        requireNonNull(arguments, "argument may not be null");
        this.arguments = arguments;
        this.location = location;
    }

    public List<Argument> getArguments() {
        return arguments;
    }

    public String getLocation() {
        return location;
    }

}
