package cucumber.api;

import java.util.List;

public interface TestStep extends Step {
    String getPattern();

    gherkin.pickles.PickleStep getPickleStep();

    List<cucumber.api.Argument> getDefinitionArgument();

    List<gherkin.pickles.Argument> getStepArgument();

    int getStepLine();

    String getStepLocation();

    String getStepText();
}
