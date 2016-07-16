package cucumber.api.event;

import cucumber.api.Result;
import cucumber.api.TestStep;

public class TestStepFinished implements Event {
    public final TestStep testStep;
    public final Result result;

    public TestStepFinished(TestStep testStep, Result result) {
        this.testStep = testStep;
        this.result = result;
    }

}
