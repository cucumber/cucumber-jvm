package cucumber.api;

import cucumber.api.event.StepDefinedEvent;
import cucumber.runtime.StepDefinition;

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
