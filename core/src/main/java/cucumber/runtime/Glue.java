package cucumber.runtime;

import cucumber.api.StepDefinitionReporter;
import gherkin.I18n;
import gherkin.formatter.model.Step;

import java.util.List;


//TODO: now that this is just basically a java bean storing values
// I don't think it needs an interface anymore...
public interface Glue {
    void addStepDefinition(StepDefinition stepDefinition) throws DuplicateStepDefinitionException;

    @Deprecated
    void addBeforeHook(HookDefinition hookDefinition);

    @Deprecated
    void addAfterHook(HookDefinition hookDefinition);

    @Deprecated
    List<HookDefinition> getBeforeHooks();

    @Deprecated
    List<HookDefinition> getAfterHooks();
    
    void addBeforeHook(HookDefinition hookDefinition, HookScope scope);

    void addAfterHook(HookDefinition hookDefinition, HookScope scope);

    List<HookDefinition> getBeforeHooks(HookScope scope);

    List<HookDefinition> getAfterHooks(HookScope scope);

    StepDefinitionMatch stepDefinitionMatch(String featurePath, Step step, I18n i18n);

    void reportStepDefinitions(StepDefinitionReporter stepDefinitionReporter);

    void removeScenarioScopedGlue();
}