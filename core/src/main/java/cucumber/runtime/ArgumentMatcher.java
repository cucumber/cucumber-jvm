package cucumber.runtime;

import gherkin.formatter.Argument;

import java.util.List;

public interface ArgumentMatcher {
    List<Argument> argumentsFrom(String stepName);
}
