package io.cucumber.core.runner;

import io.cucumber.core.api.Scenario;

interface StepDefinitionMatch {
    void runStep(Scenario scenario) throws Throwable;

    void dryRunStep(Scenario scenario) throws Throwable;

    String getCodeLocation();

}
