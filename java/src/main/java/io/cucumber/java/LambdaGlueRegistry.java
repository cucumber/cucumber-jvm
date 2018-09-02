package io.cucumber.java;

import io.cucumber.core.stepexpression.TypeRegistry;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.StepDefinition;

public interface LambdaGlueRegistry {
    ThreadLocal<LambdaGlueRegistry> INSTANCE = new ThreadLocal<LambdaGlueRegistry>();

    void addStepDefinition(Function<TypeRegistry, StepDefinition> stepDefinition);

    void addBeforeStepHookDefinition(HookDefinition beforeStepHook);

    void addAfterStepHookDefinition(HookDefinition afterStepHook);

    void addBeforeHookDefinition(HookDefinition beforeHook);

    void addAfterHookDefinition(HookDefinition afterHook);
}
