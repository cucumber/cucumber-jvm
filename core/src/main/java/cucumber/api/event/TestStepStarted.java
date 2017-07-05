package cucumber.api.event;

import cucumber.api.TestStep;

public final class TestStepStarted extends TimeStampedEvent {
    public final TestStep testStep;

    public TestStepStarted(Long timeStamp, TestStep testStep) {
        super(timeStamp);
        this.testStep = testStep;
    }

}
