package io.cucumber.core.runner;

import io.cucumber.core.backend.Step;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.plugin.event.Argument;
import io.cucumber.plugin.event.StepArgument;
import io.cucumber.plugin.event.TestCase;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

final class PickleStepTestStep extends TestStep implements io.cucumber.plugin.event.PickleStepTestStep, Step {

    private final URI uri;
    private final io.cucumber.core.gherkin.Step step;
    private final List<HookTestStep> afterStepHookSteps;
    private final List<HookTestStep> beforeStepHookSteps;
    private final PickleStepDefinitionMatch definitionMatch;

    PickleStepTestStep(
            UUID id, URI uri, io.cucumber.core.gherkin.Step step, PickleStepDefinitionMatch definitionMatch
    ) {
        this(id, uri, step, Collections.emptyList(), Collections.emptyList(), definitionMatch);
    }

    PickleStepTestStep(
            UUID id, URI uri,
            io.cucumber.core.gherkin.Step step,
            List<HookTestStep> beforeStepHookSteps,
            List<HookTestStep> afterStepHookSteps,
            PickleStepDefinitionMatch definitionMatch
    ) {
        super(id, definitionMatch);
        this.uri = uri;
        this.step = step;
        this.afterStepHookSteps = afterStepHookSteps;
        this.beforeStepHookSteps = beforeStepHookSteps;
        this.definitionMatch = definitionMatch;
    }

    @Override
    ExecutionMode run(TestCase testCase, EventBus bus, TestCaseState state, ExecutionMode executionMode) {
        ExecutionMode nextExecutionMode = executionMode;

        state.setCurrentPickleStep(this);

        for (HookTestStep before : beforeStepHookSteps) {
            nextExecutionMode = before
                    .run(testCase, bus, state, executionMode)
                    .next(nextExecutionMode);
        }

        nextExecutionMode = super.run(testCase, bus, state, nextExecutionMode)
                .next(nextExecutionMode);

        for (HookTestStep after : afterStepHookSteps) {
            nextExecutionMode = after
                    .run(testCase, bus, state, executionMode)
                    .next(nextExecutionMode);
        }

        state.clearCurrentPickleStep();

        return nextExecutionMode;
    }

    List<HookTestStep> getBeforeStepHookSteps() {
        return beforeStepHookSteps;
    }

    List<HookTestStep> getAfterStepHookSteps() {
        return afterStepHookSteps;
    }

    @Override
    public String getPattern() {
        return definitionMatch.getPattern();
    }

    @Override
    public io.cucumber.core.gherkin.Step getStep() {
        return step;
    }

    @Override
    public List<Argument> getDefinitionArgument() {
        return DefinitionArgument.createArguments(definitionMatch.getArguments());
    }

    public PickleStepDefinitionMatch getDefinitionMatch() {
        return definitionMatch;
    }

    @Override
    public StepArgument getStepArgument() {
        return step.getArgument();
    }

    @Override
    public int getStepLine() {
        return step.getLine();
    }

    @Override
    public URI getUri() {
        return uri;
    }

    @Override
    public String getStepText() {
        return step.getText();
    }

    @Override
    public String getKeyword() {
        return step.getKeyword();
    }

    @Override
    public String getText() {
        return step.getText();
    }

    @Override
    public int getLine() {
        return step.getLine();
    }
}
