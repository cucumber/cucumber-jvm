package cucumber.runtime;

import cucumber.api.Scenario;

public interface StepDefinitionMatch {
    void runStep(String language, Scenario scenario) throws Throwable;

    void dryRunStep(String language, Scenario scenario) throws Throwable;

    String getCodeLocation();

}
