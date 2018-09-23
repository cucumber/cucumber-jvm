package io.cucumber.core.backend;

public interface Glue {

    void addStepDefinition(StepDefinition stepDefinition) throws DuplicateStepDefinitionException;

    void addBeforeHook(HookDefinition hookDefinition);

    void addAfterHook(HookDefinition hookDefinition);

    void addBeforeStepHook(HookDefinition beforeStepHook);

    void addAfterStepHook(HookDefinition hookDefinition);

}
