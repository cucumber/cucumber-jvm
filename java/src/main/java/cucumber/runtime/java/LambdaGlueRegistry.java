package cucumber.runtime.java;

import cucumber.runtime.HookDefinition;
import cucumber.runtime.StepDefinition;
import io.cucumber.java.TypeRegistry;

public interface LambdaGlueRegistry {
    ThreadLocal<LambdaGlueRegistry> INSTANCE = new ThreadLocal<LambdaGlueRegistry>();

    void addStepDefinition(Function<TypeRegistry, StepDefinition> stepDefinition);

    void addBeforeHookDefinition(HookDefinition beforeHook);

    void addAfterHookDefinition(HookDefinition afterHook);
}
