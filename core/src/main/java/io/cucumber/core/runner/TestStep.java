package io.cucumber.core.runner;

import io.cucumber.core.backend.Pending;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.messages.Messages;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestStepFinished;
import io.cucumber.plugin.event.TestStepStarted;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

import static io.cucumber.core.runner.ExecutionMode.SKIP;
import static io.cucumber.core.runner.TestStepResultStatus.from;
import static io.cucumber.messages.TimeConversion.javaDurationToDuration;
import static io.cucumber.messages.TimeConversion.javaInstantToTimestamp;
import static java.time.Duration.ZERO;

abstract class TestStep implements io.cucumber.plugin.event.TestStep {

    private static final String[] TEST_ABORTED_OR_SKIPPED_EXCEPTIONS = {
            "org.junit.AssumptionViolatedException",
            "org.junit.internal.AssumptionViolatedException",
            "org.opentest4j.TestAbortedException",
            "org.testng.SkipException",
    };

    static {
        Arrays.sort(TEST_ABORTED_OR_SKIPPED_EXCEPTIONS);
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

    @Override
    public UUID getId() {
        return id;
    }

    ExecutionMode run(TestCase testCase, EventBus bus, TestCaseState state, ExecutionMode executionMode) {
        Instant startTime = bus.getInstant();
        emitTestStepStarted(testCase, bus, state.getTestExecutionId(), startTime);

        Status status;
        Throwable error = null;
        try {
            status = executeStep(state, executionMode);
        } catch (Throwable t) {
            error = t;
            status = mapThrowableToStatus(t);
        }
        Instant stopTime = bus.getInstant();
        Duration duration = Duration.between(startTime, stopTime);
        Result result = mapStatusToResult(status, error, duration);
        state.add(result);

        emitTestStepFinished(testCase, bus, state.getTestExecutionId(), stopTime, duration, result);

        return result.getStatus().is(Status.PASSED) ? executionMode : SKIP;
    }

    private void emitTestStepStarted(TestCase testCase, EventBus bus, UUID textExecutionId, Instant startTime) {
        bus.send(new TestStepStarted(startTime, testCase, this));
        bus.send(Messages.Envelope.newBuilder()
                .setTestStepStarted(Messages.TestStepStarted.newBuilder()
                        .setTestCaseStartedId(textExecutionId.toString())
                        .setTestStepId(id.toString())
                        .setTimestamp(javaInstantToTimestamp(startTime)))
                .build());
    }

    private Status executeStep(TestCaseState state, ExecutionMode executionMode) throws Throwable {
        state.setCurrentTestStepId(id);
        try {
            return executionMode.execute(stepDefinitionMatch, state);
        } finally {
            state.clearCurrentTestStepId();
        }
    }

    private Status mapThrowableToStatus(Throwable t) {
        if (t.getClass().isAnnotationPresent(Pending.class)) {
            return Status.PENDING;
        }
        if (Arrays.binarySearch(TEST_ABORTED_OR_SKIPPED_EXCEPTIONS, t.getClass().getName()) >= 0) {
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

    private void emitTestStepFinished(
            TestCase testCase, EventBus bus, UUID textExecutionId, Instant stopTime, Duration duration, Result result
    ) {
        bus.send(new TestStepFinished(stopTime, testCase, this, result));
        Messages.TestStepFinished.TestStepResult.Builder builder = Messages.TestStepFinished.TestStepResult
                .newBuilder();

        if (result.getError() != null) {
            builder.setMessage(extractStackTrace(result.getError()));
        }
        Messages.TestStepFinished.TestStepResult testResult = builder.setStatus(from(result.getStatus()))
                .setDuration(javaDurationToDuration(duration))
                .build();
        bus.send(Messages.Envelope.newBuilder()
                .setTestStepFinished(Messages.TestStepFinished.newBuilder()
                        .setTestCaseStartedId(textExecutionId.toString())
                        .setTestStepId(id.toString())
                        .setTimestamp(javaInstantToTimestamp(stopTime))
                        .setTestStepResult(testResult))
                .build());
    }

    private String extractStackTrace(Throwable error) {
        ByteArrayOutputStream s = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(s);
        error.printStackTrace(printStream);
        return new String(s.toByteArray(), StandardCharsets.UTF_8);
    }

}
