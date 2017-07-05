package cucumber.api.event;

import cucumber.api.Result;
import cucumber.api.TestStep;

public final class TestStepFinished extends TimeStampedEvent {
    public final TestStep testStep;
    public final Result result;

    public TestStepFinished(Long timeStamp, TestStep testStep, Result result) {
        super(timeStamp);
        this.testStep = testStep;
        this.result = result;
    }

}
