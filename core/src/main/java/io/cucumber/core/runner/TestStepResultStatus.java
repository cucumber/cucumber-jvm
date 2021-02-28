package io.cucumber.core.runner;

import io.cucumber.messages.Messages;
import io.cucumber.plugin.event.Status;

import java.util.HashMap;
import java.util.Map;

class TestStepResultStatus {

    private static final Map<Status, Messages.TestStepFinished.TestStepResult.Status> STATUS = new HashMap<Status, Messages.TestStepFinished.TestStepResult.Status>() {
        {
            put(Status.FAILED, Messages.TestStepFinished.TestStepResult.Status.FAILED);
            put(Status.PASSED, Messages.TestStepFinished.TestStepResult.Status.PASSED);
            put(Status.UNDEFINED, Messages.TestStepFinished.TestStepResult.Status.UNDEFINED);
            put(Status.PENDING, Messages.TestStepFinished.TestStepResult.Status.PENDING);
            put(Status.SKIPPED, Messages.TestStepFinished.TestStepResult.Status.SKIPPED);
            put(Status.AMBIGUOUS, Messages.TestStepFinished.TestStepResult.Status.AMBIGUOUS);
        }
    };

    private TestStepResultStatus() {
    }

    static Messages.TestStepFinished.TestStepResult.Status from(Status status) {
        return STATUS.getOrDefault(status, Messages.TestStepFinished.TestStepResult.Status.UNKNOWN);
    }

}
