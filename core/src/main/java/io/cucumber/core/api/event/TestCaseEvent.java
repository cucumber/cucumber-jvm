package io.cucumber.core.api.event;

public abstract class TestCaseEvent extends TimeStampedEvent {

    private final TestCase testCase;

    TestCaseEvent(Long timeStamp, long timeStampMillis, TestCase testCase) {
        super(timeStamp, timeStampMillis);
        this.testCase = testCase;
    }

    public TestCase getTestCase() {
        return testCase;
    }
}
