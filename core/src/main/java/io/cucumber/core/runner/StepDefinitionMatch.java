package io.cucumber.core.runner;

interface StepDefinitionMatch {
    void runStep(TestCaseState state) throws Throwable;

    void dryRunStep(TestCaseState state) throws Throwable;

    String getCodeLocation();

}
