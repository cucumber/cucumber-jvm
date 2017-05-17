package cucumber.api.event;

import cucumber.api.Result;
import cucumber.api.TestCase;

public final class TestCaseFinished extends TimeStampedEvent {
    public final Result result;
    public final TestCase testCase;

    public TestCaseFinished(Long timeStamp, TestCase testCase, Result result) {
        super(timeStamp);
        this.testCase = testCase;
        this.result = result;
    }

}
