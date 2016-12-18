package cucumber.runner;

import cucumber.api.HookType;
import cucumber.api.TestStep;
import cucumber.runtime.DefinitionMatch;
import cucumber.runtime.StepDefinitionMatch;
import gherkin.pickles.Argument;
import gherkin.pickles.PickleStep;

import java.util.List;

public class PickleTestStep extends TestStep {
    private String uri;
    private PickleStep step;

    public PickleTestStep(String uri, PickleStep step, DefinitionMatch definitionMatch) {
        super(definitionMatch);
        this.uri = uri;
        this.step = step;
    }

    @Override
    public boolean isHook() {
        return false;
    }

    @Override
    public PickleStep getPickleStep() {
        return step;
    }

    @Override
    public String getStepLocation() {
        return uri + ":" + Integer.toString(getStepLine());
    }

    @Override
    public int getStepLine() {
        return StepDefinitionMatch.getStepLine(step);
    }

    @Override
    public String getStepText() {
        return step.getText();
    }

    @Override
    public List<Argument> getStepArgument() {
        return step.getArgument();
    }

    @Override
    public HookType getHookType() {
        throw new UnsupportedOperationException();
    }
}
