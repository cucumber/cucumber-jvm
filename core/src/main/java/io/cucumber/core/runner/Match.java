package io.cucumber.core.runner;

import io.cucumber.core.stepexpression.Argument;

import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

abstract class Match {

    private final List<Argument> arguments;
    private final String location;
    public static final Match UNDEFINED = new UndefinedMatch();

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

    private static final class UndefinedMatch extends Match {
        UndefinedMatch() {
            super(Collections.emptyList(), null);
        }
    }
}
