package cucumber.runtime;

import gherkin.formatter.Reporter;
import gherkin.formatter.model.Step;

import java.util.ArrayList;

public interface World {
    void buildBackendWorldsAndRunBeforeHooks(Reporter reporter);

    void runAfterHooksAndDisposeBackendWorlds(Reporter reporter);
    }

    public void runUnreportedStep(String uri, Step step, Locale locale) throws Throwable {
        StepDefinitionMatch match = stepDefinitionMatch(uri, step, locale);
        if (match == null) {
            throw new CucumberException("Calling an undefined step from " + uri);
        }

        match.runStep(locale);

    void runStep(String uri, Step step, Reporter reporter, Locale locale);

    void addStepDefinition(StepDefinition stepDefinition);

    void addBeforeHook(HookDefinition hookDefinition);

    void addAfterHook(HookDefinition hookDefinition);

    List<HookDefinition> getBeforeHooks();

    List<HookDefinition> getAfterHooks();

    List<StepDefinition> getStepDefinitions();
}
