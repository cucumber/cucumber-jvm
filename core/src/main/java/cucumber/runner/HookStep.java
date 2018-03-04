package cucumber.runner;

import cucumber.api.HookType;
import cucumber.api.TestStep;
import cucumber.runtime.DefinitionMatch;
import gherkin.pickles.Argument;
import gherkin.pickles.PickleStep;

import java.util.List;

public abstract class HookStep extends TestStep {
    private final HookType hookType;

    public HookStep(HookType hookType, DefinitionMatch definitionMatch) {
        super(definitionMatch);
        this.hookType = hookType;
    }

    @Override
    public boolean isHook() {
        return true;
    }

    @Override
    public PickleStep getPickleStep() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getStepLocation() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getStepLine() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getStepText() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Argument> getStepArgument() {
        throw new UnsupportedOperationException();
    }

    @Override
    public HookType getHookType() {
        return hookType;
    }

    @Override
    public boolean startingGherkinStepType() {
        return false;
    }

    @Override
    public boolean finishingGherkinStepType() {
        return hookType == HookType.Before || hookType == HookType.AfterStep;
    }
}
