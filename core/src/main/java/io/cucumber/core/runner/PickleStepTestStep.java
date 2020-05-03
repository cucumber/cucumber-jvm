package io.cucumber.core.runner;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.gherkin.Step;
import io.cucumber.plugin.event.Argument;
import io.cucumber.plugin.event.StepArgument;
import io.cucumber.plugin.event.TestCase;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

final class PickleStepTestStep extends TestStep implements io.cucumber.plugin.event.PickleStepTestStep {

    private final URI uri;
    private final Step step;
    private final List<HookTestStep> afterStepHookSteps;
    private final List<HookTestStep> beforeStepHookSteps;
    private final PickleStepDefinitionMatch definitionMatch;

    PickleStepTestStep(UUID id, URI uri, Step step, PickleStepDefinitionMatch definitionMatch) {
        this(id, uri, step, Collections.emptyList(), Collections.emptyList(), definitionMatch);
    }

    PickleStepTestStep(UUID id, URI uri,
                       Step step,
                       List<HookTestStep> beforeStepHookSteps,
                       List<HookTestStep> afterStepHookSteps,
                       PickleStepDefinitionMatch definitionMatch) {
        super(id, definitionMatch);
        this.uri = uri;
        this.step = step;
        this.afterStepHookSteps = afterStepHookSteps;
        this.beforeStepHookSteps = beforeStepHookSteps;
        this.definitionMatch = definitionMatch;
    }

    @Override
    boolean run(TestCase testCase, EventBus bus, TestCaseState state, boolean skipSteps) {
        boolean skipNextStep = skipSteps;

        for (HookTestStep before : beforeStepHookSteps) {
            skipNextStep |= before.run(testCase, bus, state, skipSteps);
        }

        skipNextStep |= super.run(testCase, bus, state, skipNextStep);

        for (HookTestStep after : afterStepHookSteps) {
            skipNextStep |= after.run(testCase, bus, state, skipSteps);
        }

        return skipNextStep;
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
    public Step getStep() {
        return step;
    }

    @Override
    public List<Argument> getDefinitionArgument() {
        return DefinitionArgument.createArguments(definitionMatch.getArguments());
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

}
