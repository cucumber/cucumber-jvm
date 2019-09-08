package io.cucumber.core.runner;

interface StepDefinitionMatch {
    void runStep(Scenario scenario) throws Throwable;

    void dryRunStep(Scenario scenario) throws Throwable;

    String getCodeLocation();

}
