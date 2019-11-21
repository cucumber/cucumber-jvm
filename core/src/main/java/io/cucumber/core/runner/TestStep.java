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
    private final String id = UUID.randomUUID().toString();

    TestStep(StepDefinitionMatch stepDefinitionMatch) {
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

    boolean run(TestCase testCase, EventBus bus, TestCaseState state, boolean skipSteps) {
        Instant startTimeMillis = bus.getInstant();

//        if (stepDefinitionMatch instanceof PickleStepDefinitionMatch) {
//            PickleStepDefinitionMatch match = (PickleStepDefinitionMatch) stepDefinitionMatch;
////            Messages.Envelope message = makeTestStepMatchedEnvelope(testCase, match);
////            bus.send(new TestStepMatched(startTimeMillis, testCase, this, message));
//        }

        bus.send(new TestStepStarted(startTimeMillis, testCase, this));
        Status status;
        Throwable error = null;
        try {
            status = executeStep(state, skipSteps);
        } catch (Throwable t) {
            error = t;
            status = mapThrowableToStatus(t);
        }
        Instant stopTimeNanos = bus.getInstant();
        Result result = mapStatusToResult(status, error, Duration.between(startTimeMillis, stopTimeNanos));
        state.add(result);
        bus.send(new TestStepFinished(stopTimeNanos, testCase, this, result));
        return !result.getStatus().is(Status.PASSED);
    }

//    private Messages.Envelope makeTestStepMatchedEnvelope(TestCase testCase, PickleStepDefinitionMatch match) {
//        int pickleStepIndex = getPickleStepIndex(testCase);
//        Messages.Envelope message = Messages.Envelope.newBuilder()
//            .setTestStepMatched(Messages.TestStepMatched.newBuilder()
//                .setPickleId(testCase.getPickleId())
//                .setIndex(pickleStepIndex)
//                .addAllStepMatchArguments(match.getArguments()
//                    .stream()
//                    .filter(arg -> arg instanceof ExpressionArgument)
//                    .map(ExpressionArgument.class::cast)
//                    .map(arg -> Messages.StepMatchArgument.newBuilder()
//                        .setParameterTypeName(arg.getParameterTypeName())
//                        .setGroup(convert(arg.getGroup())))
//                    .map(Messages.StepMatchArgument.Builder::build)
//                    .collect(Collectors.toList()))
//            ).build();
//        return message;
//    }
//
//    private Messages.StepMatchArgument.Group convert(Group group) {
//        Messages.StepMatchArgument.Group.Builder builder = Messages.StepMatchArgument.Group.newBuilder();
//        if (group.getValue() != null) {
//            builder.setValue(group.getValue());
//        }
//        return builder
//            .setStart(group.getStart())
//            .addAllChildren(group.getChildren()
//                .stream()
//                .map(this::convert)
//                .collect(Collectors.toList()))
//            .build();
//    }
//
//    private int getPickleStepIndex(TestCase testCase) {
//        return testCase.getTestSteps()
//            .stream().filter(s -> s instanceof PickleStepTestStep)
//            .collect(Collectors.toList())
//            .indexOf(this);
//    }

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
