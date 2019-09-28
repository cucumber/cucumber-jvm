package io.cucumber.plugin.event;

import org.apiguardian.api.API;

import java.util.List;

/**
 * A pickle test step matches a line in a Gherkin scenario or background.
 */
@API(status = API.Status.STABLE)
public interface PickleStepTestStep extends TestStep {

    /**
     * The pattern or expression used to match the glue code to the Gherkin step.
     *
     * @return a pattern or expression
     */
    String getPattern();

    /**
     * The matched Gherkin step
     *
     * @return the matched step
     */
    CucumberStep getStep();

    /**
     * Returns the arguments provided to the step definition.
     * <p>
     * For example the step definition <code>Given (.*) pickles</code>
     * when matched with <code>Given 15 pickles</code> will receive
     * as argument <code>"15"</code>.
     *
     * @return argument provided to the step definition
     */
    List<Argument> getDefinitionArgument();

    /**
     * Returns arguments provided to the Gherkin step. E.g:
     * a data table or doc string.
     *
     * @return arguments provided to the gherkin step.
     */

    StepArgument getStepArgument();

    /**
     * The line in the feature file defining this step.
     *
     * @return a line number
     */
    int getStepLine();

    /**
     * A uri to to the feature of this step.
     *
     * @return a uri
     */
    String getUri();

    /**
     * The full text of the Gherkin step.
     *
     * @return the step text
     */
    String getStepText();
}
