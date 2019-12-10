package io.cucumber.core.runner;

import io.cucumber.messages.Messages;
import io.cucumber.plugin.event.Status;

import java.util.HashMap;
import java.util.Map;

class TestResultStatus {
    private static final Map<Status, Messages.TestResult.Status> STATUS = new HashMap<Status, Messages.TestResult.Status>() {{
        put(Status.FAILED, Messages.TestResult.Status.FAILED);
        put(Status.PASSED, Messages.TestResult.Status.PASSED);
        put(Status.UNDEFINED, Messages.TestResult.Status.UNDEFINED);
        put(Status.PENDING, Messages.TestResult.Status.PENDING);
        put(Status.SKIPPED, Messages.TestResult.Status.SKIPPED);
        put(Status.AMBIGUOUS, Messages.TestResult.Status.AMBIGUOUS);
    }};

    private TestResultStatus() {
    }

    static Messages.TestResult.Status from(Status status) {
        return STATUS.getOrDefault(status, Messages.TestResult.Status.UNKNOWN);
    }

}
