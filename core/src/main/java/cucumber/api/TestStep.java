package cucumber.api;

import cucumber.runtime.DefinitionMatch;
import gherkin.pickles.Argument;
import gherkin.pickles.PickleStep;

import java.util.List;

public abstract class TestStep extends Step {

    protected TestStep(DefinitionMatch definitionMatch) {
        super(definitionMatch);
    }

    public String getPattern() {
        return definitionMatch.getPattern();
    }

    public abstract PickleStep getPickleStep();

    public abstract String getStepText();

    public abstract String getStepLocation();

    public abstract int getStepLine();

    public abstract List<Argument> getStepArgument();
}
