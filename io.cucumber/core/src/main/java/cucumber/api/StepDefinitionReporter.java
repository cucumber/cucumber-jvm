package cucumber.api;

import cucumber.runtime.StepDefinition;

public interface StepDefinitionReporter {
    /**
     * Called when a step definition is defined
     *
     * @param stepDefinition the step definition
     */
    void stepDefinition(StepDefinition stepDefinition);
}
