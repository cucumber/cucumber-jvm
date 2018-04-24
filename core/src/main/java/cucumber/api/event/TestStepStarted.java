package cucumber.api.event;

import cucumber.api.TestStep;

public final class TestStepStarted extends TimeStampedEvent {
    public final TestStep testStep;
    public boolean reRunTestCase;

    public TestStepStarted(Long timeStamp, TestStep testStep, boolean reRunTestCase) {
        super(timeStamp);
        this.testStep = testStep;
        this.reRunTestCase = reRunTestCase;
    }

}
