package io.cucumber.core.plugin;

import io.cucumber.messages.types.TestStepFinished;

import java.util.List;
import java.util.Map;

class TestStepData {
    final List<TestStepFinished> beforeTestCaseSteps;
    final List<TestStepFinished> afterTestCaseSteps;
    final Map<TestStepFinished, List<TestStepFinished>> beforeStepStepsByStep;
    final Map<TestStepFinished, List<TestStepFinished>> afterStepStepsByStep;

    TestStepData(
            List<TestStepFinished> beforeTestCaseSteps,
            List<TestStepFinished> afterTestCaseSteps,
            Map<TestStepFinished, List<TestStepFinished>> beforeStepStepsByStep,
            Map<TestStepFinished, List<TestStepFinished>> afterStepStepsByStep
    ) {
        this.beforeTestCaseSteps = beforeTestCaseSteps;
        this.afterTestCaseSteps = afterTestCaseSteps;
        this.beforeStepStepsByStep = beforeStepStepsByStep;
        this.afterStepStepsByStep = afterStepStepsByStep;
    }
}
