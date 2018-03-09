package cucumber.runner;

import cucumber.api.HookType;
import cucumber.runtime.DefinitionMatch;

class HookStep extends Step implements cucumber.api.HookStep {
    private final HookType hookType;

    HookStep(HookType hookType, DefinitionMatch definitionMatch) {
        super(definitionMatch);
        this.hookType = hookType;
    }

    @Override
    public HookType getHookType() {
        return hookType;
    }

}
