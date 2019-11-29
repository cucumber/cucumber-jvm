package io.cucumber.core.runner;

import io.cucumber.core.backend.Pending;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.messages.Messages;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestStepFinished;
import io.cucumber.plugin.event.TestStepStarted;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

import static io.cucumber.core.messages.MessageHelpers.toDuration;
import static io.cucumber.core.messages.MessageHelpers.toStatus;
import static io.cucumber.core.messages.MessageHelpers.toTimestamp;
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
    private final String id = UUID.randomUUID().toString();
    private final String pickleStepId;

    TestStep(String pickleStepId, StepDefinitionMatch stepDefinitionMatch) {
        this.pickleStepId = pickleStepId;
        this.stepDefinitionMatch = stepDefinitionMatch;
    }

    @Override
    public String getCodeLocation() {
        return stepDefinitionMatch.getCodeLocation();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getPickleStepId() {
        return pickleStepId;
    }

    @Override
    public Iterable<Messages.StepMatchArgument> getStepMatchArguments() {
        return stepDefinitionMatch.getStepMatchArguments();
    }

    boolean run(TestCase testCase, EventBus bus, TestCaseState state, boolean skipSteps, String testCaseStartedId) {
        Instant startTime = bus.getInstant();

        bus.send(new TestStepStarted(startTime, testCase, this));
        sendTestStepStarted(bus, testCaseStartedId, startTime);

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

        sendTestStepFinished(bus, testCaseStartedId, stopTime, duration, result);

        return !result.getStatus().is(Status.PASSED);
    }

    private void sendTestStepStarted(EventBus bus, String testCaseStartedId, Instant startTime) {
        bus.send(Messages.Envelope.newBuilder()
            .setTestStepStarted(Messages.TestStepStarted.newBuilder()
                .setTestCaseStartedId(testCaseStartedId)
                .setTestStepId(getId())
                .setTimestamp(toTimestamp(startTime))
            ).build());
    }

    private void sendTestStepFinished(EventBus bus, String testCaseStartedId, Instant stopTime, Duration duration, Result result) {
        bus.send(Messages.Envelope.newBuilder()
            .setTestStepFinished(Messages.TestStepFinished.newBuilder()
                .setTestCaseStartedId(testCaseStartedId)
                .setTestStepId(getId())
                .setTimestamp(toTimestamp(stopTime))
                .setTestResult(Messages.TestResult.newBuilder()
                    .setStatus(toStatus(result.getStatus()))
                    .setDuration(toDuration(duration))
                )
            ).build());
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
