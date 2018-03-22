package cucumber.api;

import java.util.List;

/**
 * A test step can either represent the execution of a hook
 * or a pickle step. Each step is tied to some glue code.
 *
 * @see cucumber.api.event.TestCaseStarted
 * @see cucumber.api.event.TestCaseFinished
 */
public interface TestStep {

    /**
     * Returns a string representation of the glue code location.
     *
     * @return a string representation of the glue code location.
     */
    String getCodeLocation();

    /**
     * Returns the hook hook type.
     *
     * @return the hook type.
     * @deprecated cast to {@link HookTestStep} instead.
     */
    @Deprecated
    HookType getHookType();

    /**
     * Returns true if the test step is a hook test step
     * @return true if the test step is a hook test step
     * @deprecated type check {@link HookTestStep} or {@link PickleStepTestStep} instead.
     */
    @Deprecated
    boolean isHook();

    /**
     * The pattern or expression used to match the glue code to the Gherkin step.
     *
     * @return a pattern or expression
     * @deprecated cast to {@link PickleStepTestStep} instead.
     */
    @Deprecated
    String getPattern();

    /**
     * The matched Gherkin step as a compiled Pickle
     *
     * @return the matched step
     * @deprecated cast to {@link PickleStepTestStep} instead.
     */
    @Deprecated
    gherkin.pickles.PickleStep getPickleStep();

    /**
     * Returns the arguments provided to the step definition.
     * <p>
     * For example the step definition <code>Given (.*) pickles</code>
     * when matched with <code>Given 15 pickles</code> will receive
     * as argument <code>"15"</code>
     *
     * @return argument provided to the step definition
     * @deprecated cast to {@link PickleStepTestStep} instead.
     */
    @Deprecated
    List<Argument> getDefinitionArgument();

    /**
     * Returns arguments provided to the Gherkin step. E.g:
     * a data table or doc string.
     *
     * @return arguments provided to the gherkin step.
     * @deprecated cast to {@link PickleStepTestStep} instead.
     */
    @Deprecated
    List<gherkin.pickles.Argument> getStepArgument();

    /**
     * The line in the feature file defining this step.
     *
     * @return a line number
     * @deprecated cast to {@link PickleStepTestStep} instead.
     */
    @Deprecated
    int getStepLine();

    /**
     * A uri to to the feature and line of this step.
     *
     * @return a uri
     * @deprecated cast to {@link PickleStepTestStep} instead.
     */
    @Deprecated
    String getStepLocation();

    /**
     * The full text of the Gherkin step.
     *
     * @return the step text
     * @deprecated cast to {@link PickleStepTestStep} instead.
     */
    @Deprecated
    String getStepText();


}
