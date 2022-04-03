package io.cucumber.core.runner;

import io.cucumber.plugin.event.Status;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class TestStepResultStatus {

    private static final Map<Status, io.cucumber.messages.types.TestStepResultStatus> STATUS;

    static {
        Map<Status, io.cucumber.messages.types.TestStepResultStatus> status = new HashMap<>();
        status.put(Status.FAILED, io.cucumber.messages.types.TestStepResultStatus.FAILED);
        status.put(Status.PASSED, io.cucumber.messages.types.TestStepResultStatus.PASSED);
        status.put(Status.UNDEFINED, io.cucumber.messages.types.TestStepResultStatus.UNDEFINED);
        status.put(Status.PENDING, io.cucumber.messages.types.TestStepResultStatus.PENDING);
        status.put(Status.SKIPPED, io.cucumber.messages.types.TestStepResultStatus.SKIPPED);
        status.put(Status.AMBIGUOUS, io.cucumber.messages.types.TestStepResultStatus.AMBIGUOUS);
        STATUS = Collections.unmodifiableMap(status);
    };

    private TestStepResultStatus() {
    }

    static io.cucumber.messages.types.TestStepResultStatus from(Status status) {
        return STATUS.getOrDefault(status, io.cucumber.messages.types.TestStepResultStatus.UNKNOWN);
    }

}
