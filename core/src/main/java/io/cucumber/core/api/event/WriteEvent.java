package io.cucumber.core.api.event;

import java.time.Instant;

public final class WriteEvent extends TestCaseEvent {
    public final String text;

    public WriteEvent(Instant timeInstant, TestCase testCase, String text) {
        super(timeInstant, testCase);
        this.text = text;
    }
}
