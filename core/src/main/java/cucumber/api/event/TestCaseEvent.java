package cucumber.api.event;

import cucumber.api.TestCase;

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
