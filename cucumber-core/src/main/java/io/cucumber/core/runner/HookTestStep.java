package io.cucumber.core.runner;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.TestStepResult;
import io.cucumber.plugin.event.HookType;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.Step;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestStepFinished;
import io.cucumber.plugin.event.TestStepStarted;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Predicate;

import static io.cucumber.core.exception.UnrecoverableExceptions.rethrowIfUnrecoverable;
import static io.cucumber.core.runner.ExecutionMode.SKIP;
import static io.cucumber.core.runner.TestAbortedExceptions.createIsTestAbortedExceptionPredicate;
import static io.cucumber.core.runner.TestStepResultStatusMapper.from;
import static io.cucumber.messages.Convertor.toMessage;

final class HookTestStep extends TestStep implements io.cucumber.plugin.event.HookTestStep {

    private final Predicate<Throwable> isTestAbortedException = createIsTestAbortedExceptionPredicate();
    private final HookType hookType;
    private final HookDefinitionMatch definitionMatch;

    HookTestStep(UUID id, HookType hookType, HookDefinitionMatch definitionMatch) {
        super(id, definitionMatch);
        this.hookType = hookType;
        this.definitionMatch = definitionMatch;
    }

    @Override
    public HookType getHookType() {
        return hookType;
    }

    HookDefinitionMatch getDefinitionMatch() {
        return definitionMatch;
    }

    /**
     * Runs this hook test step with step information. This is used for
     * {@code @BeforeStep} and {@code @AfterStep} hooks to provide access to
     * step details.
     *
     * @param  testCase      the test case
     * @param  bus           the event bus
     * @param  state         the test case state
     * @param  executionMode the execution mode
     * @param  step          the step being executed (may be null for non-step
     *                       hooks)
     * @return               the next execution mode
     */
    ExecutionMode run(
            TestCase testCase,
            EventBus bus,
            TestCaseState state,
            ExecutionMode executionMode,
            Step step
    ) {
        Instant startTime = bus.getInstant();
        emitTestStepStarted(testCase, bus, state.getTestExecutionId(), startTime);

        Status status;
        Throwable error = null;
        try {
            status = executeStep(state, executionMode, step);
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
            getId().toString(),
            toMessage(startTime)));
        bus.send(envelope);
    }

    private Status executeStep(TestCaseState state, ExecutionMode executionMode, Step step) throws Throwable {
        state.setCurrentTestStepId(getId());
        try {
            if (executionMode == ExecutionMode.RUN) {
                definitionMatch.runStep(state, step);
                return Status.PASSED;
            } else if (executionMode == ExecutionMode.DRY_RUN) {
                definitionMatch.dryRunStep(state);
                return Status.PASSED;
            } else {
                return Status.SKIPPED;
            }
        } finally {
            state.clearCurrentTestStepId();
        }
    }

    private Status mapThrowableToStatus(Throwable t) {
        if (t.getClass().isAnnotationPresent(io.cucumber.core.backend.Pending.class)) {
            return Status.PENDING;
        }
        if (isTestAbortedException.test(t)) {
            return Status.SKIPPED;
        }
        return Status.FAILED;
    }

    private Result mapStatusToResult(Status status, Throwable error, Duration duration) {
        return new Result(status, duration, error);
    }

    private void emitTestStepFinished(
            TestCase testCase, EventBus bus, UUID textExecutionId, Instant stopTime, Duration duration, Result result
    ) {
        bus.send(new TestStepFinished(stopTime, testCase, this, result));

        TestStepResult testStepResult = new TestStepResult(
            toMessage(duration),
            result.getError() != null ? result.getError().getMessage() : null,
            from(result.getStatus()),
            result.getError() != null ? toMessage(result.getError()) : null);

        Envelope envelope = Envelope.of(new io.cucumber.messages.types.TestStepFinished(
            textExecutionId.toString(),
            getId().toString(),
            testStepResult,
            toMessage(stopTime)));
        bus.send(envelope);
    }

}
