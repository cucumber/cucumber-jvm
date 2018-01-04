package cucumber.runtime;

import cucumber.api.Argument;
import gherkin.pickles.PickleStep;

import java.util.List;

public interface ArgumentMatcher {
    List<Argument> argumentsFrom(PickleStep step);
}
