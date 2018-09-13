package io.cucumber.core.api.event;

public abstract class TestCaseEvent extends TimeStampedEvent {

    final TestCase testCase;

    TestCaseEvent(Long timeStamp, TestCase testCase) {
        super(timeStamp);
        this.testCase = testCase;
    }

    public TestCase getTestCase() {
        return testCase;
    }
}
