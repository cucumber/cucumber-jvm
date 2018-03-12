package cucumber.api.event;

import cucumber.api.Result;
import cucumber.api.Step;

/**
 * A test step finished event is broadcast when ever a step finishes.
 * <p>
 * A step can either be a {@link cucumber.api.TestStep} or a
 * {@link cucumber.api.HookStep} depending on what step was executed.
 * <p>
 * Each test step finished event is followed by an matching
 * {@link TestStepStarted} event for the same step.The order in which
 * these events may be expected is:
 * <p>
 * <pre>
 *     [before hook,]* [[before step hook,]* test step, [after step hook,]*]+, [after hook,]*
 * </pre>
 *
 * @see cucumber.api.TestStep
 * @see cucumber.api.HookStep
 */
public final class TestStepFinished extends TimeStampedEvent {
    public final Step testStep;
    public final Result result;

    public TestStepFinished(Long timeStamp, Step testStep, Result result) {
        super(timeStamp);
        this.testStep = testStep;
        this.result = result;
    }

}
