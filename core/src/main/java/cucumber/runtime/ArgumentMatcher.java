package cucumber.runtime;

import gherkin.pickles.PickleStep;
import io.cucumber.cucumberexpressions.Argument;

import java.util.List;

public interface ArgumentMatcher {
    List<Argument<?>> argumentsFrom(PickleStep step);
}
