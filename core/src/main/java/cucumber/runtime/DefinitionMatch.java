package cucumber.runtime;

import cucumber.api.Scenario;

import java.util.List;

public interface DefinitionMatch {
    Object runStep(String language, Scenario scenario) throws Throwable;

    Object dryRunStep(String language, Scenario scenario) throws Throwable;

    Match getMatch();

    String getPattern();

    String getCodeLocation();

    List<Argument> getArguments();

    List<String> getSnippets();
}
