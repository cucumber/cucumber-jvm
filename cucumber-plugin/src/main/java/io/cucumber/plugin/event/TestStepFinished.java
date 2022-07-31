package io.cucumber.plugin.event;

import org.apiguardian.api.API;

import java.time.Instant;
import java.util.Objects;

/**
 * A test step finished event is broadcast when ever a step finishes.
 * <p>
 * A step can either be a {@link PickleStepTestStep} or a {@link HookTestStep}
 * depending on what step was executed.
 * <p>
 * Each test step finished event is followed by an matching
 * {@link TestStepStarted} event for the same step.The order in which these
 * events may be expected is:
 * 
 * <pre>
 *     [before hook,]* [[before step hook,]* test step, [after step hook,]*]+, [after hook,]*
 * </pre>
 *
 * @see PickleStepTestStep
 * @see HookTestStep
 */

@API(status = API.Status.STABLE)
public final class TestStepFinished extends TestCaseEvent {

    private final TestStep testStep;
    private final Result result;

    public TestStepFinished(Instant timeInstant, TestCase testCase, TestStep testStep, Result result) {
        super(timeInstant, testCase);
        this.testStep = Objects.requireNonNull(testStep);
        this.result = Objects.requireNonNull(result);
    }

    public Result getResult() {
        return result;
    }

    public TestStep getTestStep() {
        return testStep;
    }

}
