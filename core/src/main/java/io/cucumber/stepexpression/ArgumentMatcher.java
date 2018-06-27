package io.cucumber.stepexpression;

import io.cucumber.messages.Messages.PickleStep;

import java.util.List;

public interface ArgumentMatcher {
    List<Argument> argumentsFrom(PickleStep step);
}
