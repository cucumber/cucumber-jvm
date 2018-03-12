package cucumber.api;

/**
 * Hooks are invoked before and after each scenario and before and
 * after each test step in a scenario.
 *
 * @see cucumber.api.event.TestCaseStarted
 * @see cucumber.api.event.TestCaseFinished
 */
public interface HookStep extends Step {

    /**
     * Returns the hook hook type.
     *
     * @return the hook type.
     */
    HookType getHookType();

}
