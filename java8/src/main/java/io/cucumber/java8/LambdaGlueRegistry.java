package io.cucumber.java8;

import io.cucumber.core.stepexpression.TypeRegistry;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.StepDefinition;

import java.util.function.Function;

interface LambdaGlueRegistry {
    ThreadLocal<LambdaGlueRegistry> INSTANCE = new ThreadLocal<>();

    void addStepDefinition(Function<TypeRegistry, StepDefinition> stepDefinition);

    void addBeforeStepHookDefinition(HookDefinition beforeStepHook);

    void addAfterStepHookDefinition(HookDefinition afterStepHook);

    void addBeforeHookDefinition(HookDefinition beforeHook);

    void addAfterHookDefinition(HookDefinition afterHook);
}
