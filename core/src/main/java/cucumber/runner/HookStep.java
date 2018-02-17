package cucumber.runner;

import cucumber.api.HookType;
import cucumber.api.Result;
import cucumber.api.Scenario;
import cucumber.api.TestStep;
import cucumber.runtime.DefinitionMatch;
import gherkin.pickles.Argument;
import gherkin.pickles.PickleStep;

import java.util.List;

public class HookStep extends TestStep {
    private final HookType hookType;

    public HookStep(HookType hookType, DefinitionMatch definitionMatch) {
        super(definitionMatch);
        this.hookType = hookType;
    }

    protected Result.Type executeStep(String language, Scenario scenario, boolean skipSteps) throws Throwable {
        if(hookType == HookType.After || hookType == HookType.Before) {
            definitionMatch.runStep(language, scenario);
            return Result.Type.PASSED;
        } else { //Either hook step is AfterStep or BeforeStep
            if(!skipSteps) {
                definitionMatch.runStep(language, scenario);
                return Result.Type.PASSED;
            } else {
                return Result.Type.SKIPPED;
            }
        }
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
}
