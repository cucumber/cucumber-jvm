package cucumber.api.event;

import cucumber.api.Step;

/**
 * A test step started event is broadcast when ever a step starts.
 *
 * A step can either be a {@link cucumber.api.TestStep} or a
 * {@link cucumber.api.HookStep} depending on what step was executed.
 *
 * Each test step started event is followed by an matching
 * {@link TestStepFinished} event for the same step.The order in
 * which these events may be expected is:
 *
 * <pre>
 *     [before hook,]* [[before step hook,]* test step, [after step hook,]*]+, [after hook,]*
 * </pre>
 *
 * @see cucumber.api.TestStep
 * @see cucumber.api.HookStep
 */
public final class TestStepStarted extends TimeStampedEvent {
    public final Step testStep;

    public TestStepStarted(Long timeStamp, Step testStep) {
        super(timeStamp);
        this.testStep = testStep;
    }

}
