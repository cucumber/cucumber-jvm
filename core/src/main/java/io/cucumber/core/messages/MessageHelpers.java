package io.cucumber.core.messages;

import io.cucumber.messages.Messages;
import io.cucumber.plugin.event.Status;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

//TODO: Make util more local to consumers
public class MessageHelpers {
    private static final Map<Status, Messages.TestResult.Status> STATUS = new HashMap<Status, Messages.TestResult.Status>() {{
        put(Status.FAILED, Messages.TestResult.Status.FAILED);
        put(Status.PASSED, Messages.TestResult.Status.PASSED);
        put(Status.UNDEFINED, Messages.TestResult.Status.UNDEFINED);
        put(Status.PENDING, Messages.TestResult.Status.PENDING);
        put(Status.SKIPPED, Messages.TestResult.Status.SKIPPED);
        put(Status.AMBIGUOUS, Messages.TestResult.Status.AMBIGUOUS);
        put(Status.UNUSED, Messages.TestResult.Status.UNKNOWN);
    }};

    private MessageHelpers() {
    }

    public static Messages.TestResult.Status toStatus(Status status) {
        return STATUS.get(status);
    }

    //TODO: This should be in messages
    public static Messages.Timestamp toTimestamp(Instant instant) {
        return Messages.Timestamp.newBuilder()
            .setSeconds(instant.getEpochSecond())
            .setNanos(instant.getNano())
            .build();
    }
    //TODO: This should be in messages
    public static Messages.Duration toDuration(Duration duration) {
        return Messages.Duration.newBuilder()
            .setSeconds(duration.getSeconds())
            .setNanos(duration.getNano())
            .build();
    }


}
