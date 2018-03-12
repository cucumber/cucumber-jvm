package cucumber.api;

import java.util.List;

/**
 * A test step matches a line in a Gherkin scenario or background.
 */
public interface TestStep extends Step {

    /**
     * The pattern or expression used to match the glue code to the Gherkin step.
     *
     * @return a pattern or expression
     */
    String getPattern();

    /**
     * The matched Gherkin step as a compiled Pickle
     *
     * @return the matched step
     */
    gherkin.pickles.PickleStep getPickleStep();


    List<cucumber.api.Argument> getDefinitionArgument();

    List<gherkin.pickles.Argument> getStepArgument();

    /**
     * The line in the feature file defining this step.
     *
     * @return a line number
     */
    int getStepLine();

    /**
     * A uri to to the feature and line of this step.
     *
     * @return a uri
     */
    String getStepLocation();

    /**
     * The full text of the Gherkin step.
     *
     * @return the step text
     */
    String getStepText();
}
