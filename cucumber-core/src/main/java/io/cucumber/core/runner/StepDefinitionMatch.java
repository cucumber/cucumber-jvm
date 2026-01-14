package io.cucumber.core.runner;

import io.cucumber.core.backend.TestCaseState;
import io.cucumber.plugin.event.Step;

interface StepDefinitionMatch {

    void runStep(TestCaseState state) throws Throwable;

    /**
     * Runs the step with step information. Used by step hooks to access step
     * details.
     */
    default void runStep(TestCaseState state, Step step) throws Throwable {
        runStep(state);
    }

    void dryRunStep(TestCaseState state) throws Throwable;

    String getCodeLocation();

}
