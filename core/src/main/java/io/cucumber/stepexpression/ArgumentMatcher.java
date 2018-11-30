package io.cucumber.stepexpression;

import gherkin.pickles.PickleStep;

import java.lang.reflect.Type;
import java.util.List;

public interface ArgumentMatcher {
    List<Argument> argumentsFrom(PickleStep step, Type... types);
}
