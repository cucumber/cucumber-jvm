package io.cucumber.core.runner;

import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.StepMatchArgument;
import io.cucumber.messages.types.StepMatchArgumentsList;
import io.cucumber.plugin.event.Group;
import io.cucumber.plugin.event.Location;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestStep;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.cucumber.core.runner.ExecutionMode.DRY_RUN;
import static io.cucumber.core.runner.ExecutionMode.RUN;
import static io.cucumber.messages.Convertor.toMessage;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

final class TestCase implements io.cucumber.plugin.event.TestCase {

    private final Pickle pickle;
    private final List<PickleStepTestStep> testSteps;
    private final ExecutionMode executionMode;
    private final List<HookTestStep> beforeHooks;
    private final List<HookTestStep> afterHooks;
    private final UUID id;

    TestCase(
            UUID id, List<PickleStepTestStep> testSteps,
            List<HookTestStep> beforeHooks,
            List<HookTestStep> afterHooks,
            Pickle pickle,
            boolean dryRun
    ) {
        this.id = id;
        this.testSteps = testSteps;
        this.beforeHooks = beforeHooks;
        this.afterHooks = afterHooks;
        this.pickle = pickle;
        this.executionMode = dryRun ? DRY_RUN : RUN;
    }

    private static io.cucumber.messages.types.Group makeMessageGroup(
            Group group
    ) {
        long start = group.getStart();
        return new io.cucumber.messages.types.Group(
            group.getChildren().stream()
                    .map(TestCase::makeMessageGroup)
                    .collect(toList()),
            start == -1 ? null : start,
            group.getValue());
    }

    void run(EventBus bus) {
        ExecutionMode nextExecutionMode = this.executionMode;
        emitTestCaseMessage(bus);

        Instant start = bus.getInstant();
        UUID executionId = bus.generateId();
        emitTestCaseStarted(bus, start, executionId);

        TestCaseState state = new TestCaseState(bus, executionId, this);

        for (HookTestStep before : beforeHooks) {
            nextExecutionMode = before
                    .run(this, bus, state, executionMode)
                    .next(nextExecutionMode);
        }

        for (PickleStepTestStep step : testSteps) {
            nextExecutionMode = step
                    .run(this, bus, state, nextExecutionMode)
                    .next(nextExecutionMode);
        }

        for (HookTestStep after : afterHooks) {
            nextExecutionMode = after
                    .run(this, bus, state, executionMode)
                    .next(nextExecutionMode);
        }

        Instant stop = bus.getInstant();
        Duration duration = Duration.between(start, stop);
        Status status = Status.valueOf(state.getStatus().name());
        Result result = new Result(status, duration, state.getError());
        emitTestCaseFinished(bus, executionId, stop, result);
    }

    @Override
    public Integer getLine() {
        return pickle.getLocation().getLine();
    }

    @Override
    public Location getLocation() {
        return pickle.getLocation();
    }

    @Override
    public String getKeyword() {
        return pickle.getKeyword();
    }

    @Override
    public String getName() {
        return pickle.getName();
    }

    @Override
    public String getScenarioDesignation() {
        return fileColonLine(getLocation().getLine()) + " # " + getName();
    }

    private String fileColonLine(Integer line) {
        return pickle.getUri().getSchemeSpecificPart() + ":" + line;
    }

    @Override
    public List<String> getTags() {
        return pickle.getTags();
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
    public URI getUri() {
        return pickle.getUri();
    }

    @Override
    public UUID getId() {
        return id;
    }

    private void emitTestCaseMessage(EventBus bus) {
        Envelope envelope = Envelope.of(new io.cucumber.messages.types.TestCase(
            id.toString(),
            pickle.getId(),
            getTestSteps()
                    .stream()
                    .map(this::createTestStep)
                    .collect(toList()),
            null));
        bus.send(envelope);
    }

    private io.cucumber.messages.types.TestStep createTestStep(TestStep pluginTestStep) {
        // public TestStep(String hookId, String id, String pickleStepId,
        // List<String> stepDefinitionIds, List<StepMatchArgumentsList>
        // stepMatchArgumentsLists) {
        String id = pluginTestStep.getId().toString();
        String hookId = null;
        String pickleStepId = null;
        List<StepMatchArgumentsList> stepMatchArgumentsLists = emptyList();
        List<String> stepDefinitionIds = emptyList();

        if (pluginTestStep instanceof HookTestStep) {
            HookTestStep hookTestStep = (HookTestStep) pluginTestStep;
            HookDefinitionMatch definitionMatch = hookTestStep.getDefinitionMatch();
            CoreHookDefinition hookDefinition = definitionMatch.getHookDefinition();
            hookId = hookDefinition.getId().toString();
        } else if (pluginTestStep instanceof PickleStepTestStep) {
            PickleStepTestStep pickleStep = (PickleStepTestStep) pluginTestStep;
            pickleStepId = pickleStep.getStep().getId();
            stepMatchArgumentsLists = getStepMatchArguments(pickleStep);
            StepDefinition stepDefinition = pickleStep.getDefinitionMatch().getStepDefinition();
            if (stepDefinition instanceof CoreStepDefinition) {
                CoreStepDefinition coreStepDefinition = (CoreStepDefinition) stepDefinition;
                stepDefinitionIds = singletonList(coreStepDefinition.getId().toString());
            }
        }

        return new io.cucumber.messages.types.TestStep(hookId, id, pickleStepId, stepDefinitionIds,
            stepMatchArgumentsLists);
    }

    public List<StepMatchArgumentsList> getStepMatchArguments(PickleStepTestStep pickleStep) {
        if (pickleStep.getDefinitionMatch() instanceof UndefinedPickleStepDefinitionMatch) {
            return emptyList();
        }

        return singletonList(pickleStep.getDefinitionArgument()
                .stream()
                .map(arg -> new StepMatchArgument(makeMessageGroup(arg.getGroup()), arg.getParameterTypeName()))
                .collect(Collectors.collectingAndThen(toList(), StepMatchArgumentsList::new)));
    }

    private void emitTestCaseStarted(EventBus bus, Instant start, UUID executionId) {
        bus.send(new TestCaseStarted(start, this));
        Envelope envelope = Envelope.of(new io.cucumber.messages.types.TestCaseStarted(
            0L,
            executionId.toString(),
            id.toString(),
            Thread.currentThread().getName(),
            toMessage(start)));
        bus.send(envelope);
    }

    private void emitTestCaseFinished(
            EventBus bus, UUID executionId, Instant stop, Result result
    ) {
        bus.send(new TestCaseFinished(stop, this, result));
        Envelope envelope = Envelope.of(new io.cucumber.messages.types.TestCaseFinished(executionId.toString(),
            toMessage(stop), false));
        bus.send(envelope);
    }

}
