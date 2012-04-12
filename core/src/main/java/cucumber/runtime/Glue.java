package cucumber.runtime;

import gherkin.I18n;
import gherkin.formatter.model.Step;

import java.io.File;
import java.io.IOException;
import java.util.List;

import cucumber.runtime.HookDefinition;
import cucumber.runtime.StaticHookDefinition;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.StepDefinitionMatch;


//TODO: now that this is just basically a java bean storing values
// I don't think it needs an interface anymore...
public interface Glue {

    void addStepDefinition(StepDefinition stepDefinition);

    void addBeforeHook(HookDefinition hookDefinition);

    void addAfterHook(HookDefinition hookDefinition);

    void addBeforeClassHook(StaticHookDefinition hookDefinition);
    
    void addAfterClassHook(StaticHookDefinition hookDefinition);
    
    List<HookDefinition> getBeforeHooks();

    List<HookDefinition> getAfterHooks();

    List<StaticHookDefinition> getBeforeClassHooks();

    List<StaticHookDefinition> getAfterClassHooks();
    
    StepDefinitionMatch stepDefinitionMatch(String uri, Step step, I18n i18n);

    void writeStepdefsJson(List<String> featurePaths, File dotCucumber) throws IOException;
}
