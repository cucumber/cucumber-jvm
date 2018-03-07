package cucumber.api.event;

import cucumber.api.Step;

public final class TestStepStarted extends TimeStampedEvent {
    public final Step testStep;

    public TestStepStarted(Long timeStamp, Step testStep) {
        super(timeStamp);
        this.testStep = testStep;
    }

}
