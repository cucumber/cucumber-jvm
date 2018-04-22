package cucumber.runtime.java;

import cucumber.runtime.Glue;
import cucumber.runtime.HookDefinition;
import cucumber.runtime.StepDefinition;

public interface LambdaGlueRegistry {
    ThreadLocal<LambdaGlueRegistry> INSTANCE = new ThreadLocal<LambdaGlueRegistry>();
    ThreadLocal<Glue> GLUE = new ThreadLocal<Glue>();

    void addStepDefinition(Glue glue, StepDefinition stepDefinition);

    void addBeforeHookDefinition(Glue glue, HookDefinition beforeHook);

    void addAfterHookDefinition(Glue glue, HookDefinition afterHook);
}
