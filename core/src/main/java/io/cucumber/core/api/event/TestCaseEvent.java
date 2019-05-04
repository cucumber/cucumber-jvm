package io.cucumber.core.api.event;

import java.time.Instant;

public abstract class TestCaseEvent extends TimeStampedEvent {

    private final TestCase testCase;

    @Deprecated
    TestCaseEvent(Long timeStamp, long timeStampMillis, TestCase testCase) {
        super(timeStamp, timeStampMillis);
        this.testCase = testCase;
    }
    
    TestCaseEvent(Instant timeInstant, TestCase testCase) {
        super(timeInstant);
        this.testCase = testCase;
    }

    public TestCase getTestCase() {
        return testCase;
    }
}
