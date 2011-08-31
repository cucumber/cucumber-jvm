package cucumber.runtime;

import gherkin.formatter.model.Step;

import java.util.List;

public interface Backend {
    List<StepDefinition> getStepDefinitions();

    /**
     * Invoked before a new scenario starts. Implementations should do any necessary
     * setup of new, isolated state here.
     */
    void newWorld();

    void disposeWorld();

    String getSnippet(Step step);

    List<HookDefinition> getBeforeHooks();

    List<HookDefinition> getAfterHooks();
}
