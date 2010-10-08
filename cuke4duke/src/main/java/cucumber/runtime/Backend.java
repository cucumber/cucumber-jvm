package cucumber.runtime;

import cucumber.StepDefinition;

import java.util.List;

public interface Backend {
    List<StepDefinition> getStepDefinitions();
}
