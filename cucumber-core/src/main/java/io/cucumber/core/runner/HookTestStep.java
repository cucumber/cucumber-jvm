package io.cucumber.core.runner;

import io.cucumber.plugin.event.HookType;

import java.util.UUID;

/**
 * Represents a hook test step (Before, After, BeforeStep, AfterStep).
 * <p>
 * For {@code @BeforeStep} and {@code @AfterStep} hooks, step information is
 * passed through the parent class's
 * {@link TestStep#run(io.cucumber.plugin.event.TestCase, io.cucumber.core.eventbus.EventBus, TestCaseState, ExecutionMode, io.cucumber.plugin.event.Step)}
 * method.
 */
final class HookTestStep extends TestStep implements io.cucumber.plugin.event.HookTestStep {

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

}
