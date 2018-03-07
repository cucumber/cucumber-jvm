package cucumber.api.event;

import cucumber.api.Result;
import cucumber.api.Step;

public final class TestStepFinished extends TimeStampedEvent {
    public final Step testStep;
    public final Result result;

    public TestStepFinished(Long timeStamp, Step testStep, Result result) {
        super(timeStamp);
        this.testStep = testStep;
        this.result = result;
    }

}
