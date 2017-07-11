package cucumber.runtime;

import io.cucumber.cucumberexpressions.Argument;

import java.util.List;

public interface ArgumentMatcher {
    List<Argument<?>> argumentsFrom(String stepName);
}
