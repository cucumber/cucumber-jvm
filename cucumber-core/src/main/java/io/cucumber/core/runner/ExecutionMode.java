package io.cucumber.core.runner;

import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.Step;

enum ExecutionMode {

    RUN {
        @Override
        Status execute(StepDefinitionMatch stepDefinitionMatch, TestCaseState state, Step step) throws Throwable {
            stepDefinitionMatch.runStep(state, step);
            return Status.PASSED;
        }
    },
    DRY_RUN {
        @Override
        Status execute(StepDefinitionMatch stepDefinitionMatch, TestCaseState state, Step step) throws Throwable {
            stepDefinitionMatch.dryRunStep(state);
            return Status.PASSED;
        }
    },
    SKIP {
        @Override
        Status execute(StepDefinitionMatch stepDefinitionMatch, TestCaseState state, Step step) {
            return Status.SKIPPED;
        }
    };

    abstract Status execute(StepDefinitionMatch stepDefinitionMatch, TestCaseState state, Step step) throws Throwable;

    ExecutionMode next(ExecutionMode current) {
        return current == SKIP ? current : this;
    }
}
