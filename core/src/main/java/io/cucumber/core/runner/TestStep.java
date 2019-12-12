package io.cucumber.core.runner;

import io.cucumber.core.backend.Pending;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestStepFinished;
import io.cucumber.plugin.event.TestStepStarted;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

import static java.time.Duration.ZERO;

abstract class TestStep implements io.cucumber.plugin.event.TestStep {
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
    private final UUID id;

    TestStep(UUID id, StepDefinitionMatch stepDefinitionMatch) {
        this.id = id;
        this.stepDefinitionMatch = stepDefinitionMatch;
    }

    @Override
    public String getCodeLocation() {
        return stepDefinitionMatch.getCodeLocation();
    }

    boolean run(TestCase testCase, EventBus bus, TestCaseState state, boolean skipSteps, UUID textExecutionId) {
        Instant startTime = bus.getInstant();
        bus.send(new TestStepStarted(startTime, testCase, this));
        Status status;
        Throwable error = null;
        try {
            status = executeStep(state, skipSteps);
        } catch (Throwable t) {
            error = t;
            status = mapThrowableToStatus(t);
        }
        Instant stopTime = bus.getInstant();
        Duration duration = Duration.between(startTime, stopTime);
        Result result = mapStatusToResult(status, error, duration);
        state.add(result);
        bus.send(new TestStepFinished(stopTime, testCase, this, result));
        return !result.getStatus().is(Status.PASSED);
    }

    private Status executeStep(TestCaseState state, boolean skipSteps) throws Throwable {
        if (!skipSteps) {
            stepDefinitionMatch.runStep(state);
            return Status.PASSED;
        } else {
            stepDefinitionMatch.dryRunStep(state);
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
