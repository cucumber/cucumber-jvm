package cucumber.api;

import cucumber.runtime.DefinitionMatch;

public abstract class HookStep extends Step {

    protected HookStep(DefinitionMatch definitionMatch) {
        super(definitionMatch);
    }

    public abstract HookType getHookType();

}
