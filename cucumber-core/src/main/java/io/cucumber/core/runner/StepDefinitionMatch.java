package io.cucumber.core.runner;

import io.cucumber.core.backend.TestCaseState;
import io.cucumber.plugin.event.Step;

interface StepDefinitionMatch {

    /**
     * Runs the step. The step parameter provides step information for hooks and
     * may be null.
     */
    void runStep(TestCaseState state, Step step) throws Throwable;

    void dryRunStep(TestCaseState state) throws Throwable;

    String getCodeLocation();

}
