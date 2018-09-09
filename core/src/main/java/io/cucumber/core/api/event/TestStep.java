package io.cucumber.core.api.event;

import io.cucumber.core.api.event.TestCaseFinished;
import io.cucumber.core.api.event.TestCaseStarted;

/**
 * A test step can either represent the execution of a hook
 * or a pickle step. Each step is tied to some glue code.
 *
 * @see TestCaseStarted
 * @see TestCaseFinished
 */
public interface TestStep {

    /**
     * Returns a string representation of the glue code location.
     *
     * @return a string representation of the glue code location.
     */
    String getCodeLocation();


}
