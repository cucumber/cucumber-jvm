package cucumber.runner;

import cucumber.api.HookType;
import cucumber.runtime.DefinitionMatch;

public class HookStep extends cucumber.api.HookStep {
    private final HookType hookType;

    public HookStep(HookType hookType, DefinitionMatch definitionMatch) {
        super(definitionMatch);
        this.hookType = hookType;
    }

    @Override
    public HookType getHookType() {
        return hookType;
    }

}
