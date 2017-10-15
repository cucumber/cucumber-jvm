package cucumber.runtime.java;

import cucumber.runtime.HookDefinition;
import cucumber.runtime.StepDefinition;
import io.cucumber.cucumberexpressions.ParameterTypeRegistry;

public interface LambdaGlueRegistry {
    ThreadLocal<LambdaGlueRegistry> INSTANCE = new ThreadLocal<LambdaGlueRegistry>();

    void addStepDefinition(Function<ParameterTypeRegistry, StepDefinition> stepDefinition);

    void addBeforeHookDefinition(HookDefinition beforeHook);

    void addAfterHookDefinition(HookDefinition afterHook);
}
