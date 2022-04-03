package io.cucumber.core.runner;

import io.cucumber.core.backend.Pending;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.TestStepResult;
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
import java.util.UUID;
import java.util.function.Predicate;

import static io.cucumber.core.exception.UnrecoverableExceptions.rethrowIfUnrecoverable;
import static io.cucumber.core.runner.ExecutionMode.SKIP;
import static io.cucumber.core.runner.TestAbortedExceptions.createIsTestAbortedExceptionPredicate;
import static io.cucumber.core.runner.TestStepResultStatus.from;
import static io.cucumber.messages.TimeConversion.javaDurationToDuration;
import static io.cucumber.messages.TimeConversion.javaInstantToTimestamp;
import static java.time.Duration.ZERO;

abstract class TestStep implements io.cucumber.plugin.event.TestStep {

    private final Predicate<Throwable> isTestAbortedException = createIsTestAbortedExceptionPredicate();
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
            rethrowIfUnrecoverable(t);
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
        Envelope envelope = Envelope.of(new io.cucumber.messages.types.TestStepStarted(
            textExecutionId.toString(),
            id.toString(),
            javaInstantToTimestamp(startTime)));
        bus.send(envelope);
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
        if (isTestAbortedException.test(t)) {
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

        TestStepResult testStepResult = new TestStepResult(
            javaDurationToDuration(duration),
            result.getError() != null ? extractStackTrace(result.getError()) : null,
            from(result.getStatus()));

        Envelope envelope = Envelope.of(new io.cucumber.messages.types.TestStepFinished(
            textExecutionId.toString(),
            id.toString(),
            testStepResult,
            javaInstantToTimestamp(stopTime)));
        bus.send(envelope);
    }

    private String extractStackTrace(Throwable error) {
        ByteArrayOutputStream s = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(s);
        error.printStackTrace(printStream);
        return new String(s.toByteArray(), StandardCharsets.UTF_8);
    }

}
