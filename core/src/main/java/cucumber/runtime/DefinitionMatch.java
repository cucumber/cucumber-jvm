package cucumber.runtime;

import cucumber.api.Scenario;

import java.util.List;

public interface DefinitionMatch {
    void runStep(String language, Scenario scenario) throws Throwable;

    void dryRunStep(String language, Scenario scenario) throws Throwable;

    Match getMatch();

    String getPattern();

    String getCodeLocation();

    List<Argument> getArguments();
}
