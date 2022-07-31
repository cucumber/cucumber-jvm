package io.cucumber.plugin.event;

import org.apiguardian.api.API;

/**
 * Represents a step in a scenario.
 */
@API(status = API.Status.STABLE)
public interface Step {

    /**
     * Returns this Gherkin step argument. Can be either a data table or doc
     * string.
     *
     * @return a step argument, null if absent
     */
    StepArgument getArgument();

    /**
     * Returns this steps keyword. I.e. Given, When, Then.
     *
     * @return     step key word
     * @deprecated use {@link #getKeyword()} instead
     */
    default String getKeyWord() {
        return getKeyword();
    }

    /**
     * Returns this steps keyword. I.e. Given, When, Then.
     *
     * @return step key word
     */
    String getKeyword();

    /**
     * Returns this steps text.
     *
     * @return this steps text
     */
    String getText();

    /**
     * Line in the source this step is located in.
     *
     * @return step line number
     */
    int getLine();

    /**
     * Location of this step in in the source.
     *
     * @return location in the source
     */
    Location getLocation();

}
