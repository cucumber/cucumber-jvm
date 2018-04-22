package cucumber.runtime;

import cucumber.api.StepDefinitionReporter;
import gherkin.pickles.PickleStep;

import java.util.List;


//TODO: now that this is just basically a java bean storing values
// I don't think it needs an interface anymore...
// TODO: would be nice to separate the methods in this interface into a GlueBuilder (use before all tests) and GlueContent (used during test execution) but requires changes in many areas so will leave for now
public interface Glue {
    
    Glue clone();
    
    //<editor-fold desc="pre test execution">
    void addStepDefinition(StepDefinition stepDefinition) throws DuplicateStepDefinitionException;

    void reportStepDefinitions(StepDefinitionReporter stepDefinitionReporter);
    //</editor-fold>
    
    //<editor-fold desc=pre test execution, but also during execution. Called by loadGlue but also JavaBackend.buildWorld()">
    void addBeforeHook(HookDefinition hookDefinition);

    void addAfterHook(HookDefinition hookDefinition);
    
    void removeScenarioScopedGlue();
    //</editor-fold>

    //<editor-fold desc="state changing methods, called during execution of PickleEvents">    
    List<HookDefinition> getBeforeHooks();

    List<HookDefinition> getAfterHooks();

    StepDefinitionMatch stepDefinitionMatch(String featurePath, PickleStep step);    
    //</editor-fold>
}
