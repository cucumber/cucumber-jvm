package cucumber.java.hook;

import cucumber.api.Scenario;
import cucumber.runtime.HookDefinition;

public class AroundStepHook extends Hook {
    protected CallableStep step;

    public AroundStepHook(HookDefinition hookDefinition) {
        super(hookDefinition);
    }

    public void invokeHook(Scenario scenario, CallableStep step) throws Throwable {
        this.step = step;
        super.invokeHook(scenario);
    }

    public void skipHook() throws Throwable {
        step.call();
    }
}
