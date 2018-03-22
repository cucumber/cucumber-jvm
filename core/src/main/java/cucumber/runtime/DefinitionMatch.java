package cucumber.runtime;

import cucumber.api.Scenario;

public interface DefinitionMatch {
    void runStep(String language, Scenario scenario) throws Throwable;

    void dryRunStep(String language, Scenario scenario) throws Throwable;

    String getCodeLocation();

}
