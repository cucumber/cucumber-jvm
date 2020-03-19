package io.cucumber.core.runner;

import io.cucumber.messages.Messages;
import io.cucumber.plugin.event.Status;

import java.util.HashMap;
import java.util.Map;

class TestStepResultStatus {
    private static final Map<Status, Messages.TestStepResult.Status> STATUS = new HashMap<Status, Messages.TestStepResult.Status>() {{
        put(Status.FAILED, Messages.TestStepResult.Status.FAILED);
        put(Status.PASSED, Messages.TestStepResult.Status.PASSED);
        put(Status.UNDEFINED, Messages.TestStepResult.Status.UNDEFINED);
        put(Status.PENDING, Messages.TestStepResult.Status.PENDING);
        put(Status.SKIPPED, Messages.TestStepResult.Status.SKIPPED);
        put(Status.AMBIGUOUS, Messages.TestStepResult.Status.AMBIGUOUS);
    }};

    private TestStepResultStatus() {
    }

    static Messages.TestStepResult.Status from(Status status) {
        return STATUS.getOrDefault(status, Messages.TestStepResult.Status.UNKNOWN);
    }

}
