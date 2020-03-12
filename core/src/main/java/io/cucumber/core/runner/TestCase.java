package io.cucumber.core.runner;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.messages.Messages;
import io.cucumber.messages.Messages.Envelope;
import io.cucumber.messages.Messages.StepMatchArgument;
import io.cucumber.messages.Messages.TestCase.TestStep.StepMatchArgumentsList;
import io.cucumber.plugin.event.Group;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestStep;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static io.cucumber.core.runner.TestStepResultStatus.from;
import static io.cucumber.messages.TimeConversion.javaDurationToDuration;
import static io.cucumber.messages.TimeConversion.javaInstantToTimestamp;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

final class TestCase implements io.cucumber.plugin.event.TestCase {
    private final Pickle pickle;
    private final List<PickleStepTestStep> testSteps;
    private final boolean dryRun;
    private final List<HookTestStep> beforeHooks;
    private final List<HookTestStep> afterHooks;
    private final UUID id;

    TestCase(UUID id, List<PickleStepTestStep> testSteps,
             List<HookTestStep> beforeHooks,
             List<HookTestStep> afterHooks,
             Pickle pickle,
             boolean dryRun) {
        this.id = id;
        this.testSteps = testSteps;
        this.beforeHooks = beforeHooks;
        this.afterHooks = afterHooks;
        this.pickle = pickle;
        this.dryRun = dryRun;
    }

    void run(EventBus bus) {
        boolean skipNextStep = this.dryRun;
        emitTestCaseMessage(bus);

        Instant start = bus.getInstant();
        UUID executionId = bus.generateId();
        emitTestCaseStarted(bus, start, executionId);

        TestCaseState state = new TestCaseState(bus, executionId, this);

        for (HookTestStep before : beforeHooks) {
            skipNextStep |= before.run(this, bus, state, dryRun);
        }

        for (PickleStepTestStep step : testSteps) {
            skipNextStep |= step.run(this, bus, state, skipNextStep);
        }

        for (HookTestStep after : afterHooks) {
            after.run(this, bus, state, dryRun);
        }

        Instant stop = bus.getInstant();
        Duration duration = Duration.between(start, stop);
        Status status = Status.valueOf(state.getStatus().name());
        Result result = new Result(status, duration, state.getError());
        emitTestCaseFinished(bus, executionId, stop, duration, status, result);
    }

    @Override
    public List<TestStep> getTestSteps() {
        List<TestStep> testSteps = new ArrayList<>(beforeHooks);
        for (PickleStepTestStep step : this.testSteps) {
            testSteps.addAll(step.getBeforeStepHookSteps());
            testSteps.add(step);
            testSteps.addAll(step.getAfterStepHookSteps());
        }
        testSteps.addAll(afterHooks);
        return testSteps;
    }

    @Override
    public String getName() {
        return pickle.getName();
    }

    @Override
    public String getScenarioDesignation() {
        return fileColonLine(getLine()) + " # " + getName();
    }

    @Override
    public URI getUri() {
        return pickle.getUri();
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public Integer getLine() {
        return pickle.getLocation().getLine();
    }

    @Override
    public String getKeyword() {
        return pickle.getKeyword();
    }

    private String fileColonLine(Integer line) {
        return pickle.getUri().getSchemeSpecificPart() + ":" + line;
    }

    @Override
    public List<String> getTags() {
        return pickle.getTags();
    }

    private void emitTestCaseMessage(EventBus bus) {
        bus.send(Envelope.newBuilder()
            .setTestCase(Messages.TestCase.newBuilder()
                .setId(id.toString())
                .setPickleId(pickle.getId())
                .addAllTestSteps(getTestSteps()
                    .stream()
                    .map(this::createTestStep)
                    .collect(toList())
                )
            ).build()
        );
    }

    private Messages.TestCase.TestStep createTestStep(TestStep testStep) {
        Messages.TestCase.TestStep.Builder testStepBuilder = Messages.TestCase.TestStep
            .newBuilder()
            .setId(testStep.getId().toString());

        if (testStep instanceof HookTestStep) {
            HookTestStep hookTestStep = (HookTestStep) testStep;
            testStepBuilder.setHookId(hookTestStep.getId().toString());
        } else if (testStep instanceof PickleStepTestStep) {
            PickleStepTestStep pickleStep = (PickleStepTestStep) testStep;
            testStepBuilder
                .addAllStepDefinitionIds(singletonList(pickleStep.getId().toString()))
                .setPickleStepId(pickleStep.getStep().getId())
                .addAllStepMatchArgumentsLists(getStepMatchArguments(pickleStep));
        }

        return testStepBuilder.build();
    }

    public Iterable<StepMatchArgumentsList> getStepMatchArguments(PickleStepTestStep pickleStep) {
        return pickleStep.getDefinitionArgument().stream()
            .map(arg -> StepMatchArgumentsList.newBuilder()
                .addStepMatchArguments(StepMatchArgument.newBuilder()
                    .setParameterTypeName(arg.getParameterTypeName())
                    .setGroup(makeMessageGroup(arg.getGroup()))
                    .build())
                .build()
            ).collect(toList());
    }

    private static StepMatchArgument.Group makeMessageGroup(Group group) {
        StepMatchArgument.Group.Builder builder = StepMatchArgument.Group.newBuilder();
        if (group == null) {
            return builder.build();
        }

        if (group.getValue() != null) {
            builder.setValue(group.getValue());
        }
        return builder
            //TODO: We can't represent undefined / missing matches.
            .setStart(group.getStart())
            .addAllChildren(group.getChildren().stream()
                .map(TestCase::makeMessageGroup)
                .collect(toList()))
            .build();
    }

    private void emitTestCaseStarted(EventBus bus, Instant start, UUID executionId) {
        bus.send(new TestCaseStarted(start, this));
        bus.send(Envelope.newBuilder()
            .setTestCaseStarted(Messages.TestCaseStarted.newBuilder()
                .setId(executionId.toString())
                .setTestCaseId(id.toString())
                .setTimestamp(javaInstantToTimestamp(start))).build());
    }

    private void emitTestCaseFinished(EventBus bus, UUID executionId, Instant stop, Duration duration, Status status, Result result) {
        bus.send(new TestCaseFinished(stop, this, result));
        Messages.TestStepResult.Builder testResultBuilder = Messages.TestStepResult.newBuilder()
            .setStatus(from(status))
            .setDuration(javaDurationToDuration(duration));

        if (result.getError() != null) {
            testResultBuilder.setMessage(toString(result.getError()));
        }

        bus.send(Envelope.newBuilder()
            .setTestCaseFinished(Messages.TestCaseFinished.newBuilder()
                .setTestCaseStartedId(executionId.toString())
                .setTimestamp(javaInstantToTimestamp(stop)))
            .build());
    }

    private static String toString(Throwable error) {
        StringWriter stringWriter = new StringWriter();
        error.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }
}
