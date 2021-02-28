package io.cucumber.plugin.event;

import org.apiguardian.api.API;

import java.util.UUID;

/**
 * A test step can either represent the execution of a hook or a pickle step.
 * Each step is tied to some glue code.
 *
 * @see TestCaseStarted
 * @see TestCaseFinished
 */

@API(status = API.Status.STABLE)
public interface TestStep {

    /**
     * Returns a string representation of the glue code location.
     *
     * @return a string representation of the glue code location.
     */
    String getCodeLocation();

    UUID getId();

}
