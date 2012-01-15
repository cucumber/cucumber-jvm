package cucumber.runtime;

import gherkin.formatter.Reporter;
import gherkin.formatter.model.Step;

import java.util.List;
import java.util.Locale;
import java.util.Set;


public interface World {
    void buildBackendContextAndRunBeforeHooks(Reporter reporter, Set<String> tags);

    void runAfterHooksAndDisposeBackendContext(Reporter reporter, Set<String> tags);

    void runStep(String uri, Step step, Reporter reporter, Locale locale);

    void runUnreportedStep(String file, Locale locale, String stepKeyword, String stepName, int line) throws Throwable;

    void addStepDefinition(StepDefinition stepDefinition);

    void addBeforeHook(HookDefinition hookDefinition);

    void addAfterHook(HookDefinition hookDefinition);

    List<HookDefinition> getBeforeHooks();

    List<HookDefinition> getAfterHooks();

    List<StepDefinition> getStepDefinitions();

}
