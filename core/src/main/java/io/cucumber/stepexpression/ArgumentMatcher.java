package io.cucumber.stepexpression;

import gherkin.pickles.PickleStep;

import java.util.List;

public interface ArgumentMatcher {
    List<Argument> argumentsFrom(PickleStep step);
}
