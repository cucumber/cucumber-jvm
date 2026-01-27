package io.cucumber.java;

import org.apiguardian.api.API;

/**
 * Represents a step in a scenario. BeforeStep or AfterStep hooks that declare a
 * parameter of this type will receive an instance of this class providing
 * information about the step being executed.
 * <p>
 * Note: This class is not intended to be used to create reports. To create
 * custom reports use the {@code io.cucumber.plugin.Plugin} class. The plugin
 * system provides a much richer access to Cucumber than hooks could provide.
 *
 * @see BeforeStep
 * @see AfterStep
 */
@API(status = API.Status.STABLE)
public final class Step {

    private final io.cucumber.core.backend.Step delegate;

    Step(io.cucumber.core.backend.Step delegate) {
        this.delegate = delegate;
    }

    /**
     * Returns this step's keyword. I.e. Given, When, Then.
     *
     * @return step keyword
     */
    public String getKeyword() {
        return delegate.getKeyword();
    }


    /**
     * Returns this step's text.
     *
     * @return this step's text
     */
    public String getText() {
        return delegate.getText();
    }

    /**
     * Line in the feature file this step is located in.
     *
     * @return step line number
     */
    public int getLine() {
        return delegate.getLine();
    }

}
