package cucumber.api.event;

import cucumber.api.TestCase;

public class TestCaseStarted implements Event {
    public final TestCase testCase;

    public TestCaseStarted(TestCase testCase) {
        this.testCase = testCase;
    }

}
