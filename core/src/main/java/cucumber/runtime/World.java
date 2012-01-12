package cucumber.runtime;

import gherkin.formatter.Reporter;
import gherkin.formatter.model.DataTableRow;
import gherkin.formatter.model.DocString;
import gherkin.formatter.model.Step;
import java.util.Locale;
import java.util.List;


public interface World {
    void buildBackendWorldsAndRunBeforeHooks(Reporter reporter);

    void runAfterHooksAndDisposeBackendWorlds(Reporter reporter);

    void runStep(String uri, Step step, Reporter reporter, Locale locale);

    void runUnreportedStep(String file, Locale locale, String stepKeyword, String stepName, int line, List<DataTableRow> dataTableRows, DocString docString) throws Throwable;

    void addStepDefinition(StepDefinition stepDefinition);

    void addBeforeHook(HookDefinition hookDefinition);

    void addAfterHook(HookDefinition hookDefinition);

    List<HookDefinition> getBeforeHooks();

    List<HookDefinition> getAfterHooks();

    List<StepDefinition> getStepDefinitions();

}
