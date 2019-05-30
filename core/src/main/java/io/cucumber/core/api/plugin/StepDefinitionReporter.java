package io.cucumber.core.api.plugin;

import io.cucumber.core.api.event.StepDefinedEvent;
import io.cucumber.core.backend.StepDefinition;

/**
 * @deprecated in favor of {@link StepDefinedEvent}, as Lambda Step Definitions are not reported through this class.
 */
@Deprecated
public interface StepDefinitionReporter extends Plugin {
    /**
     * Called when a step definition is defined
     *
     * @param stepDefinition the step definition
     */
    void stepDefinition(StepDefinition stepDefinition);
}
