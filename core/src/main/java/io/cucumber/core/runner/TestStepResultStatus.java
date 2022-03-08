package io.cucumber.core.runner;

import io.cucumber.plugin.event.Status;

import java.util.HashMap;
import java.util.Map;

class TestStepResultStatus {

    private static final Map<Status, io.cucumber.messages.types.TestStepResultStatus> STATUS = new HashMap<>() {
        {
            put(Status.FAILED, io.cucumber.messages.types.TestStepResultStatus.FAILED);
            put(Status.PASSED, io.cucumber.messages.types.TestStepResultStatus.PASSED);
            put(Status.UNDEFINED, io.cucumber.messages.types.TestStepResultStatus.UNDEFINED);
            put(Status.PENDING, io.cucumber.messages.types.TestStepResultStatus.PENDING);
            put(Status.SKIPPED, io.cucumber.messages.types.TestStepResultStatus.SKIPPED);
            put(Status.AMBIGUOUS, io.cucumber.messages.types.TestStepResultStatus.AMBIGUOUS);
        }
    };

    private TestStepResultStatus() {
    }

    static io.cucumber.messages.types.TestStepResultStatus from(Status status) {
        return STATUS.getOrDefault(status, io.cucumber.messages.types.TestStepResultStatus.UNKNOWN);
    }

}
