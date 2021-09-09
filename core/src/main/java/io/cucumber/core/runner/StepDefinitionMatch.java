package io.cucumber.core.runner;

import io.cucumber.core.backend.TestCaseState;

interface StepDefinitionMatch {

    void runStep(TestCaseState state) throws Throwable;

    void dryRunStep(TestCaseState state) throws Throwable;

    String getCodeLocation();

}
