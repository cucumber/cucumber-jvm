package io.cucumber.core.runner;

import io.cucumber.event.HookType;

final class HookTestStep extends TestStep implements io.cucumber.event.HookTestStep {
    private final HookType hookType;

    HookTestStep(HookType hookType, HookDefinitionMatch definitionMatch) {
        super(definitionMatch);
        this.hookType = hookType;
    }

    @Override
    public HookType getHookType() {
        return hookType;
    }

}
