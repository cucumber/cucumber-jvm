package io.cucumber.stepexpression;

import cucumber.messages.Pickles.PickleStep;

import java.util.List;

public interface ArgumentMatcher {
    List<Argument> argumentsFrom(PickleStep step);
}
