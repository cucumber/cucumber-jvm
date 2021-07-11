package io.cucumber.core.runner;

import io.cucumber.messages.types.TestStepResult;
import io.cucumber.plugin.event.Status;

import java.util.HashMap;
import java.util.Map;

class TestStepResultStatus {

    private static final Map<Status, TestStepResult.Status> STATUS = new HashMap<Status, TestStepResult.Status>() {
        {
            put(Status.FAILED, TestStepResult.Status.FAILED);
            put(Status.PASSED, TestStepResult.Status.PASSED);
            put(Status.UNDEFINED, TestStepResult.Status.UNDEFINED);
            put(Status.PENDING, TestStepResult.Status.PENDING);
            put(Status.SKIPPED, TestStepResult.Status.SKIPPED);
            put(Status.AMBIGUOUS, TestStepResult.Status.AMBIGUOUS);
        }
    };

    private TestStepResultStatus() {
    }

    static TestStepResult.Status from(Status status) {
        return STATUS.getOrDefault(status, TestStepResult.Status.UNKNOWN);
    }

}
