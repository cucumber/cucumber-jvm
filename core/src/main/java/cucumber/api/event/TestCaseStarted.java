package cucumber.api.event;

import cucumber.api.TestCase;

public final class TestCaseStarted extends TimeStampedEvent implements TestCaseEvent {
    public final TestCase testCase;

    public TestCaseStarted(Long timeStamp, TestCase testCase) {
        super(timeStamp);
        this.testCase = testCase;
    }

}
