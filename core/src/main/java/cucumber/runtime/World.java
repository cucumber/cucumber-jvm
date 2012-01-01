package cucumber.runtime;

import gherkin.formatter.Reporter;
import gherkin.formatter.model.Step;

import java.util.List;
import java.util.Locale;

public interface World {
    void buildBackendWorldsAndRunBeforeHooks(Reporter reporter);

    void runAfterHooksAndDisposeBackendWorlds(Reporter reporter);

    void runStep(String uri, Step step, Reporter reporter, Locale locale);

    void addStepDefinition(StepDefinition stepDefinition);

    void addBeforeHook(HookDefinition hookDefinition);

    void addAfterHook(HookDefinition hookDefinition);

    List<HookDefinition> getBeforeHooks();

    List<HookDefinition> getAfterHooks();

    List<StepDefinition> getStepDefinitions();
}
