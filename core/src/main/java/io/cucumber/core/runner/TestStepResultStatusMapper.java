package io.cucumber.core.runner;

import io.cucumber.messages.types.TestStepResultStatus;
import io.cucumber.plugin.event.Status;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static io.cucumber.messages.types.TestStepResultStatus.*;

class TestStepResultStatusMapper {

    private static final Map<Status, TestStepResultStatus> STATUS;

    static {
        Map<Status, TestStepResultStatus> status = new HashMap<>();
        status.put(Status.FAILED, FAILED);
        status.put(Status.PASSED, PASSED);
        status.put(Status.UNDEFINED, UNDEFINED);
        status.put(Status.PENDING, PENDING);
        status.put(Status.SKIPPED, SKIPPED);
        status.put(Status.AMBIGUOUS, AMBIGUOUS);
        STATUS = Collections.unmodifiableMap(status);
    };

    private TestStepResultStatusMapper() {
    }

    static TestStepResultStatus from(Status status) {
        return STATUS.getOrDefault(status, UNKNOWN);
    }

}
