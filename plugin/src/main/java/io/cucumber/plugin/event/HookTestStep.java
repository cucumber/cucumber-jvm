package io.cucumber.plugin.event;

import org.apiguardian.api.API;

/**
 * Hooks are invoked before and after each scenario and before and after each
 * gherkin step in a scenario.
 *
 * @see TestCaseStarted
 * @see TestCaseFinished
 */
@API(status = API.Status.STABLE)
public interface HookTestStep extends TestStep {

    /**
     * Returns the hook hook type.
     *
     * @return the hook type.
     */
    HookType getHookType();

}
