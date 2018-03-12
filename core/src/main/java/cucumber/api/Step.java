package cucumber.api;

/**
 * A test step can either represent the execution of a hook
 * or a test step. Each step is tied to some glue code.
 *
 * @see cucumber.api.event.TestCaseStarted
 * @see cucumber.api.event.TestCaseFinished
 */
public interface Step {

    /**
     * Returns a string representation of the glue code location.
     *
     * @return a string representation of the glue code location.
     */
    String getCodeLocation();

}
