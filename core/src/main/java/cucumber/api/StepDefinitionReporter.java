package cucumber.api;

import cucumber.runtime.StepDefinition;

public interface StepDefinitionReporter {
    /**
     * Called when a step definition is defined
     *
     * @param stepDefinition
     */
    void stepDefinition(StepDefinition stepDefinition);
}
