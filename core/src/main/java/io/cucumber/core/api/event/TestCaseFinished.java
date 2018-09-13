package io.cucumber.core.api.event;

public final class TestCaseFinished extends TestCaseEvent {
    public final Result result;
    public final TestCase testCase;

    public TestCaseFinished(Long timeStamp, TestCase testCase, Result result) {
        super(timeStamp, testCase);
        this.testCase = testCase;
        this.result = result;
    }

}
