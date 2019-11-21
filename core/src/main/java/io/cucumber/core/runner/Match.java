package io.cucumber.core.runner;

import io.cucumber.core.stepexpression.Argument;
import io.cucumber.messages.Messages;
import io.cucumber.messages.Messages.StepMatchArgument;

import java.util.List;

import static java.util.Objects.requireNonNull;

abstract class Match {

    private final Iterable<StepMatchArgument> stepMatchArguments;
    private final List<Argument> arguments;
    private final String location;

    Match(Iterable<Messages.StepMatchArgument> stepMatchArguments, List<Argument> arguments, String location) {
        this.stepMatchArguments = stepMatchArguments;
        requireNonNull(arguments, "argument may not be null");
        this.arguments = arguments;
        this.location = location;
    }

    public List<Argument> getArguments() {
        return arguments;
    }

    public Iterable<StepMatchArgument> getStepMatchArguments() {
        return stepMatchArguments;
    }

    public String getLocation() {
        return location;
    }
}
