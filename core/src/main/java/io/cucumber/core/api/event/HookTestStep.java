package io.cucumber.core.api.event;

import io.cucumber.core.api.event.HookType;
import io.cucumber.core.api.event.TestCaseFinished;
import io.cucumber.core.api.event.TestCaseStarted;
import io.cucumber.core.api.event.TestStep;

/**
 * Hooks are invoked before and after each scenario and before and
 * after each gherkin step in a scenario.
 *
 * @see TestCaseStarted
 * @see TestCaseFinished
 */
public interface HookTestStep extends TestStep {

    /**
     * Returns the hook hook type.
     *
     * @return the hook type.
     */
    HookType getHookType();

}
