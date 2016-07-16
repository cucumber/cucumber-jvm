package cucumber.api.event;

import cucumber.api.TestStep;

public class TestStepStarted implements Event {
    public final TestStep testStep;

    public TestStepStarted(TestStep testStep) {
        this.testStep = testStep;
    }

}
