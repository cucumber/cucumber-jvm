package io.cucumber.plugin.event;

import org.apiguardian.api.API;

/**
 * Represents a step in a scenario.
 */
@API(status = API.Status.STABLE)
public interface CucumberStep {
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
     * @return step key word
     */
    String getKeyWord();

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
    int getStepLine();
}
