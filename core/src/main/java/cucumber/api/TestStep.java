package cucumber.api;

import gherkin.pickles.Argument;
import gherkin.pickles.PickleStep;

import java.util.List;

public interface TestStep extends Step {
    String getPattern();

    PickleStep getPickleStep();

    List<Argument> getStepArgument();

    int getStepLine();

    String getStepLocation();

    String getStepText();
}
