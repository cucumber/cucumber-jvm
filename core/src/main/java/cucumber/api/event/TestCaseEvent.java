package cucumber.api.event;

import cucumber.api.TestCase;

abstract class TestCaseEvent extends TimeStampedEvent {

    final TestCase testCase;

    TestCaseEvent(Long timeStamp, TestCase testCase) {
        super(timeStamp);
        this.testCase = testCase;
    }
}
