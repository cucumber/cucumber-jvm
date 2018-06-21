package cucumber.api;

import cucumber.messages.Pickles.PickleDocString;
import cucumber.messages.Pickles.PickleStep;
import cucumber.messages.Pickles.PickleTable;

import java.util.List;

/**
 * A pickle test step matches a line in a Gherkin scenario or background.
 */
public interface PickleStepTestStep extends TestStep {

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
    PickleStep getPickleStep();


    /**
     * Returns the arguments provided to the step definition.
     * <p>
     * For example the step definition <code>Given (.*) pickles</code>
     * when matched with <code>Given 15 pickles</code> will receive
     * as argument <code>"15"</code>
     *
     * @return argument provided to the step definition
     */
    List<Argument> getDefinitionArgument();

    /**
     * @return DocString provided to the gherkin step (if any).
     */
    PickleDocString getDocString();

    /**
     * @return PickleTable provided to the gherkin step (if any).
     */
    PickleTable getDataTable();

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
