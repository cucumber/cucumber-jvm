package cucumber.runner;

import cucumber.api.Argument;
import cucumber.api.HookType;
import cucumber.runtime.HookDefinitionMatch;
import gherkin.pickles.PickleStep;

import java.util.List;

class HookTestStep extends TestStep implements cucumber.api.HookTestStep {
    private final HookType hookType;

    HookTestStep(HookType hookType, HookDefinitionMatch definitionMatch) {
        super(definitionMatch);
        this.hookType = hookType;
    }

    @Override
    public HookType getHookType() {
        return hookType;
    }

    @Override
    @Deprecated
    public boolean isHook() {
        return true;
    }

    @Override
    @Deprecated
    public String getPattern() {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public PickleStep getPickleStep() {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public List<Argument> getDefinitionArgument() {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public List<gherkin.pickles.Argument> getStepArgument() {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public int getStepLine() {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public String getStepLocation() {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public String getStepText() {
        throw new UnsupportedOperationException();
    }

}
