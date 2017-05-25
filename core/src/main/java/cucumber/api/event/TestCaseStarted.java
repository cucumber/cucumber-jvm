package cucumber.api.event;

import cucumber.api.TestCase;

public class TestCaseStarted extends TimeStampedEvent {
    public final TestCase testCase;

    public TestCaseStarted(Long timeStamp, TestCase testCase) {
        super(timeStamp);
        this.testCase = testCase;
    }

}
