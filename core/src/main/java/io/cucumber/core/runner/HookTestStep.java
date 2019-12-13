package io.cucumber.core.runner;

import io.cucumber.plugin.event.HookType;

import java.util.UUID;

final class HookTestStep extends TestStep implements io.cucumber.plugin.event.HookTestStep {
    private final HookType hookType;

    HookTestStep(UUID id, HookType hookType, HookDefinitionMatch definitionMatch) {
        super(id, definitionMatch);
        this.hookType = hookType;
    }

    @Override
    public HookType getHookType() {
        return hookType;
    }

}
