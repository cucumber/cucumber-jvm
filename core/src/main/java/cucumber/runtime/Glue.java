package cucumber.runtime;

import cucumber.api.StepDefinitionReporter;
import gherkin.pickles.PickleStep;

import java.util.List;


//TODO: now that this is just basically a java bean storing values
// I don't think it needs an interface anymore...
public interface Glue {

    void addStepDefinition(StepDefinition stepDefinition) throws DuplicateStepDefinitionException;

    void addBeforeHook(HookDefinition hookDefinition);

    void addAfterHook(HookDefinition hookDefinition);

    void addBeforeStepHook(HookDefinition beforeStepHook);

    void addAfterStepHook(HookDefinition hookDefinition);

    List<HookDefinition> getBeforeHooks();

    List<HookDefinition> getAfterHooks();

    List<HookDefinition> getAfterStepHooks();

    List<HookDefinition> getBeforeStepHooks();

    PickleStepDefinitionMatch stepDefinitionMatch(String featurePath, PickleStep step);

    void reportStepDefinitions(StepDefinitionReporter stepDefinitionReporter);

    void removeScenarioScopedGlue();
}
