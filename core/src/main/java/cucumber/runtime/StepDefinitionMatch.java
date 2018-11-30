package cucumber.runtime;

import cucumber.api.Scenario;

public interface StepDefinitionMatch {
    void runStep(Scenario scenario) throws Throwable;

    void dryRunStep(Scenario scenario) throws Throwable;

    String getCodeLocation();

}
