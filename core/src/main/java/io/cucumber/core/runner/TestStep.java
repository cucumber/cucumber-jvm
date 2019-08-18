package io.cucumber.core.runner;

import io.cucumber.core.backend.Pending;
import io.cucumber.core.event.Result;
import io.cucumber.core.event.Status;
import io.cucumber.core.event.TestCase;
import io.cucumber.core.event.TestStepFinished;
import io.cucumber.core.event.TestStepStarted;
import io.cucumber.core.eventbus.EventBus;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

import static java.time.Duration.ZERO;

abstract class TestStep implements io.cucumber.core.event.TestStep {
    private static final String[] ASSUMPTION_VIOLATED_EXCEPTIONS = {
        "org.junit.AssumptionViolatedException",
        "org.junit.internal.AssumptionViolatedException",
        "org.opentest4j.TestAbortedException",
        "org.testng.SkipException",
    };

    static {
        Arrays.sort(ASSUMPTION_VIOLATED_EXCEPTIONS);
    }

    private final StepDefinitionMatch stepDefinitionMatch;

    TestStep(StepDefinitionMatch stepDefinitionMatch) {
        this.stepDefinitionMatch = stepDefinitionMatch;
    }

    @Override
    public String getCodeLocation() {
        return stepDefinitionMatch.getCodeLocation();
    }

    /**
     * Runs a test step.
     *
     * @param testCase
     * @param bus
     * @param scenario
     * @param skipSteps
     * @return true iff subsequent skippable steps should be skipped
     */
    boolean run(TestCase testCase, EventBus bus, Scenario scenario, boolean skipSteps) {
        Instant startTimeMillis = bus.getInstant();
        bus.send(new TestStepStarted(startTimeMillis, testCase, this));
        Status status;
        Throwable error = null;
        try {
            status = executeStep(scenario, skipSteps);
        } catch (Throwable t) {
            error = t;
            status = mapThrowableToStatus(t);
        }
        Instant stopTimeNanos = bus.getInstant();
        Result result = mapStatusToResult(status, error, Duration.between(startTimeMillis, stopTimeNanos));
        scenario.add(result);
        bus.send(new TestStepFinished(stopTimeNanos, testCase, this, result));
        return !result.getStatus().is(Status.PASSED);
    }

    private Status executeStep(Scenario scenario, boolean skipSteps) throws Throwable {
        if (!skipSteps) {
            stepDefinitionMatch.runStep(scenario);
            return Status.PASSED;
        } else {
            stepDefinitionMatch.dryRunStep(scenario);
            return Status.SKIPPED;
        }
    }

    private Status mapThrowableToStatus(Throwable t) {
        if (t.getClass().isAnnotationPresent(Pending.class)) {
            return Status.PENDING;
        }
        if (Arrays.binarySearch(ASSUMPTION_VIOLATED_EXCEPTIONS, t.getClass().getName()) >= 0) {
            return Status.SKIPPED;
        }
        if (t.getClass() == UndefinedStepDefinitionException.class) {
            return Status.UNDEFINED;
        }
        if (t.getClass() == AmbiguousStepDefinitionsException.class) {
            return Status.AMBIGUOUS;
        }
        return Status.FAILED;
    }

    private Result mapStatusToResult(Status status, Throwable error, Duration duration) {
        if (status == Status.UNDEFINED) {
            return new Result(status, ZERO, null);
        }
        return new Result(status, duration, error);
    }
}
