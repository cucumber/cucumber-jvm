package io.cucumber.core.runner;

import io.cucumber.plugin.event.HookType;

import java.util.UUID;

final class HookTestStep extends TestStep implements io.cucumber.plugin.event.HookTestStep {

    private final HookType hookType;
    private final HookDefinitionMatch definitionMatch;
    private PickleStepTestStep relatedTestStep;

    HookTestStep(UUID id, HookType hookType, HookDefinitionMatch definitionMatch) {
        super(id, definitionMatch);
        this.hookType = hookType;
        this.definitionMatch = definitionMatch;
    }

    @Override
    public HookType getHookType() {
        return hookType;
    }

    public void setRelatedTestStep(PickleStepTestStep step) {
        this.relatedTestStep = step;
    }

    @Override
    public PickleStepTestStep getRelatedTestStep() {
        return relatedTestStep;
    }

    HookDefinitionMatch getDefinitionMatch() {
        return definitionMatch;
    }

}
