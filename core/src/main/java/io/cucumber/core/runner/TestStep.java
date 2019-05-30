package io.cucumber.core.runner;

import io.cucumber.core.backend.Pending;
import io.cucumber.core.api.event.Result;
import io.cucumber.core.api.event.TestCase;
import io.cucumber.core.api.event.TestStepFinished;
import io.cucumber.core.api.event.TestStepStarted;
import io.cucumber.core.backend.StepDefinitionMatch;
import io.cucumber.core.event.EventBus;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;

abstract class TestStep implements io.cucumber.core.api.event.TestStep {
    private static final String[] ASSUMPTION_VIOLATED_EXCEPTIONS = {
        "org.junit.AssumptionViolatedException",
        "org.junit.internal.AssumptionViolatedException",
        "org.testng.SkipException"
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
        Result.Type status;
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
        return !result.is(Result.Type.PASSED);
    }

    private Result.Type executeStep(Scenario scenario, boolean skipSteps) throws Throwable {
        if (!skipSteps) {
            stepDefinitionMatch.runStep(scenario);
            return Result.Type.PASSED;
        } else {
            stepDefinitionMatch.dryRunStep(scenario);
            return Result.Type.SKIPPED;
        }
    }

    private Result.Type mapThrowableToStatus(Throwable t) {
        if (t.getClass().isAnnotationPresent(Pending.class)) {
            return Result.Type.PENDING;
        }
        if (Arrays.binarySearch(ASSUMPTION_VIOLATED_EXCEPTIONS, t.getClass().getName()) >= 0) {
            return Result.Type.SKIPPED;
        }
        if (t.getClass() == UndefinedStepDefinitionException.class) {
            return Result.Type.UNDEFINED;
        }
        if (t.getClass() == AmbiguousStepDefinitionsException.class) {
            return Result.Type.AMBIGUOUS;
        }
        return Result.Type.FAILED;
    }

    private Result mapStatusToResult(Result.Type status, Throwable error, Duration duration) {
        if (status == Result.Type.UNDEFINED) {
            return Result.UNDEFINED;
        }
        return new Result(status, duration, error);
    }
}
