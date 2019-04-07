package io.cucumber.core.api.event;

public final class TestCaseFinished extends TestCaseEvent {
    public final Result result;
    public final TestCase testCase;

    @Deprecated
    public TestCaseFinished(Long timeStamp, TestCase testCase, Result result) {
        this(timeStamp, 0, testCase, result);
    }

    public TestCaseFinished(Long timeStamp, long timeStampMillis, TestCase testCase, Result result) {
        super(timeStamp, timeStampMillis, testCase);
        this.testCase = testCase;
        this.result = result;
    }

}
