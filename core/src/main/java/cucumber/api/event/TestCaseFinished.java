package cucumber.api.event;

import cucumber.api.Result;
import cucumber.api.TestCase;

public class TestCaseFinished implements Event {
    public final Result result;
    public final TestCase testCase;

    public TestCaseFinished(TestCase testCase, Result result) {
        this.testCase = testCase;
        this.result = result;
    }

}
