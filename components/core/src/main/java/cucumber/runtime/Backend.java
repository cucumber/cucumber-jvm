package cucumber.runtime;

import java.util.List;

public interface Backend {
    List<StepDefinition> getStepDefinitions();

    /**
     * Invoked before a new scenario starts. Implementations should do any necessary
     * setup of new, isolated state here.
     */
    void newScenario();

    void disposeScenario();
}
