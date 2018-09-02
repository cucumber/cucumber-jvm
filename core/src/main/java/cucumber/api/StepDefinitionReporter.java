package cucumber.api;

import io.cucumber.core.backend.StepDefinition;

public interface StepDefinitionReporter extends Plugin {
    /**
     * Called when a step definition is defined
     *
     * @param stepDefinition the step definition
     */
    void stepDefinition(StepDefinition stepDefinition);
}
