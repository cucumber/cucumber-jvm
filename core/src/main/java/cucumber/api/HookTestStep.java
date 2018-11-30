package cucumber.api;

/**
 * Hooks are invoked before and after each scenario and before and
 * after each gherkin step in a scenario.
 *
 * @see cucumber.api.event.TestCaseStarted
 * @see cucumber.api.event.TestCaseFinished
 */
public interface HookTestStep extends TestStep {

    /**
     * Returns the hook hook type.
     *
     * @return the hook type.
     */
    HookType getHookType();

}
