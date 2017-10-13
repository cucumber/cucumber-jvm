package cucumber.runtime.java;

import cucumber.runtime.HookDefinition;
import cucumber.runtime.StepDefinition;

public interface LambdaGlueRegistry {
    ThreadLocal<LambdaGlueRegistry> INSTANCE = new ThreadLocal<LambdaGlueRegistry>();

    void addStepDefinition(StepDefinition stepDefinition);

    void addBeforeHookDefinition(HookDefinition beforeHook);

    void addAfterHookDefinition(HookDefinition afterHook);
}
