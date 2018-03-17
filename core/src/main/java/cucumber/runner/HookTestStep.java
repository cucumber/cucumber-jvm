package cucumber.runner;

import cucumber.api.HookType;
import cucumber.runtime.DefinitionMatch;

class HookTestStep extends TestStep implements cucumber.api.HookTestStep {
    private final HookType hookType;

    HookTestStep(HookType hookType, DefinitionMatch definitionMatch) {
        super(definitionMatch);
        this.hookType = hookType;
    }

    @Override
    public HookType getHookType() {
        return hookType;
    }

}
