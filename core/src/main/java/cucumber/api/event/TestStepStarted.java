package cucumber.api.event;

import cucumber.api.HookTestStep;
import cucumber.api.PickleStepTestStep;
import cucumber.api.TestStep;

/**
 * A test step started event is broadcast when ever a step starts.
 *
 * A step can either be a {@link PickleStepTestStep} or a
 * {@link HookTestStep} depending on what step was executed.
 *
 * Each test step started event is followed by an matching
 * {@link TestStepFinished} event for the same step.The order in
 * which these events may be expected is:
 *
 * <pre>
 *     [before hook,]* [[before step hook,]* test step, [after step hook,]*]+, [after hook,]*
 * </pre>
 *
 * @see PickleStepTestStep
 * @see HookTestStep
 */
public final class TestStepStarted extends TimeStampedEvent {
    public final TestStep testStep;

    public TestStepStarted(Long timeStamp, TestStep testStep) {
        super(timeStamp);
        this.testStep = testStep;
    }

}
