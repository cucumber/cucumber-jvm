package cucumber.runtime;

import cucumber.runtime.io.ResourceLoader;
import gherkin.I18n;
import gherkin.formatter.model.Step;

import java.io.IOException;
import java.net.URL;
import java.util.List;


//TODO: now that this is just basically a java bean storing values
// I don't think it needs an interface anymore...
public interface Glue {

    void addStepDefinition(StepDefinition stepDefinition) throws DuplicateStepDefinitionException;

    void addBeforeHook(HookDefinition hookDefinition);

    void addAfterHook(HookDefinition hookDefinition);

    List<HookDefinition> getBeforeHooks();

    List<HookDefinition> getAfterHooks();

    StepDefinitionMatch stepDefinitionMatch(String featurePath, Step step, I18n i18n);

    void writeStepdefsJson(ResourceLoader resourceLoader, List<String> featurePaths, URL dotCucumber);
}
