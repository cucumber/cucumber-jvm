package io.cucumber.plugin.event;

import org.apiguardian.api.API;

import java.time.Instant;
import java.util.Objects;

/**
 * A test step started event is broadcast when ever a step starts.
 * <p>
 * A step can either be a {@link PickleStepTestStep} or a {@link HookTestStep}
 * depending on what step was executed.
 * <p>
 * Each test step started event is followed by an matching
 * {@link TestStepFinished} event for the same step.The order in which these
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
public final class TestStepStarted extends TestCaseEvent {

    private final TestStep testStep;

    public TestStepStarted(Instant timeInstant, TestCase testCase, TestStep testStep) {
        super(timeInstant, testCase);
        this.testStep = Objects.requireNonNull(testStep);
    }

    public TestStep getTestStep() {
        return testStep;
    }

}
