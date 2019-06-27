package io.cucumber.core.event;

import java.time.Instant;

public abstract class TestCaseEvent extends TimeStampedEvent {

    private final TestCase testCase;

    TestCaseEvent(Instant timeInstant, TestCase testCase) {
        super(timeInstant);
        this.testCase = testCase;
    }

    public TestCase getTestCase() {
        return testCase;
    }
}
