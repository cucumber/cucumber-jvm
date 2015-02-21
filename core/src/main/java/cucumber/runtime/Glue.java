package cucumber.runtime;

import cucumber.api.StepDefinitionReporter;
import gherkin.I18n;
import gherkin.formatter.model.Step;

import java.util.List;


//TODO: now that this is just basically a java bean storing values
// I don't think it needs an interface anymore...
public interface Glue {

    void addStepDefinition(StepDefinition stepDefinition) throws DuplicateStepDefinitionException;

    void addBeforeHook(HookDefinition hookDefinition);

    void addAfterHook(HookDefinition hookDefinition);
    
    void addBeforeStepHook(StepHookDefinition hookDefinition);
    
    void addAfterStepHook(StepHookDefinition hookDefinition);

    List<HookDefinition> getBeforeHooks();

    List<HookDefinition> getAfterHooks();
    
    List<StepHookDefinition> getBeforeStepHooks();
    
    List<StepHookDefinition> getAfterStepHooks();

    StepDefinitionMatch stepDefinitionMatch(String featurePath, Step step, I18n i18n);

    void reportStepDefinitions(StepDefinitionReporter stepDefinitionReporter);

    void removeScenarioScopedGlue();
}