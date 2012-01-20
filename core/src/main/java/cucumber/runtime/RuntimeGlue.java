package cucumber.runtime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import cucumber.io.FileResourceLoader;
import cucumber.runtime.autocomplete.MetaStepdef;
import cucumber.runtime.autocomplete.StepdefGenerator;
import cucumber.runtime.converters.LocalizedXStreams;
import cucumber.runtime.model.CucumberFeature;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Step;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static cucumber.runtime.model.CucumberFeature.load;
import static java.util.Collections.emptyList;

public class RuntimeGlue implements Glue {
    private static final List<Object> NO_FILTERS = emptyList();
    private final LocalizedXStreams localizedXStreams = new LocalizedXStreams();

    private final List<StepDefinition> stepDefinitions = new ArrayList<StepDefinition>();
    private final List<HookDefinition> beforeHooks = new ArrayList<HookDefinition>();
    private final List<HookDefinition> afterHooks = new ArrayList<HookDefinition>();

    private final UndefinedStepsTracker tracker;

    public RuntimeGlue(UndefinedStepsTracker tracker) {
        this.tracker = tracker;
    }


    @Override
    public void addStepDefinition(StepDefinition stepDefinition) {
        stepDefinitions.add(stepDefinition);
    }

    @Override
    public void addBeforeHook(HookDefinition hookDefinition) {
        beforeHooks.add(hookDefinition);
        Collections.sort(beforeHooks, new HookComparator(true));
    }

    @Override
    public void addAfterHook(HookDefinition hookDefinition) {
        afterHooks.add(hookDefinition);
        Collections.sort(afterHooks, new HookComparator(false));
    }

    @Override
    public List<HookDefinition> getBeforeHooks() {
        return beforeHooks;
    }

    @Override
    public List<HookDefinition> getAfterHooks() {
        return afterHooks;
    }

    @Override
    public StepDefinitionMatch stepDefinitionMatch(String uri, Step step, Locale locale) {
        List<StepDefinitionMatch> matches = stepDefinitionMatches(uri, step);
        try {
            if (matches.size() == 0) {
                tracker.addUndefinedStep(step, locale);
                return null;
            }
            if (matches.size() == 1) {
                return matches.get(0);
            } else {
                throw new AmbiguousStepDefinitionsException(matches);
            }
        } finally {
            tracker.storeStepKeyword(step, locale);
        }
    }

    private List<StepDefinitionMatch> stepDefinitionMatches(String uri, Step step) {
        List<StepDefinitionMatch> result = new ArrayList<StepDefinitionMatch>();
        for (StepDefinition stepDefinition : stepDefinitions) {
            List<Argument> arguments = stepDefinition.matchedArguments(step);
            if (arguments != null) {
                result.add(new StepDefinitionMatch(arguments, stepDefinition, uri, step, localizedXStreams));
            }
        }
        return result;
    }

    /**
     * This is the second entry to running features
     *
     * @param featurePaths
     * @param dotCucumber
     * @throws java.io.IOException
     */
    public void writeStepdefsJson(List<String> featurePaths, File dotCucumber) throws IOException {
        List<CucumberFeature> features = load(new FileResourceLoader(), featurePaths, NO_FILTERS);
        List<MetaStepdef> metaStepdefs = new StepdefGenerator().generate(stepDefinitions, features);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(metaStepdefs);

        FileWriter metaJson = new FileWriter(new File(dotCucumber, "stepdefs.json"));
        metaJson.append(json);
        metaJson.close();
    }
}
