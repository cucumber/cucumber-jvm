package cucumber.runtime.java;

import io.cucumber.stepexpression.TypeRegistry;
import cucumber.runtime.HookDefinition;
import cucumber.runtime.StepDefinition;

public interface LambdaGlueRegistry {
    ThreadLocal<LambdaGlueRegistry> INSTANCE = new ThreadLocal<LambdaGlueRegistry>();

    void addStepDefinition(Function<TypeRegistry, StepDefinition> stepDefinition);

    void addBeforeStepHookDefinition(HookDefinition beforeStepHook);

    void addAfterStepHookDefinition(HookDefinition afterStepHook);

    void addBeforeHookDefinition(HookDefinition beforeHook);

    void addAfterHookDefinition(HookDefinition afterHook);
}
