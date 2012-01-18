package cucumber.runtime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import cucumber.io.ClasspathResourceLoader;
import cucumber.io.ResourceLoader;
import cucumber.runtime.autocomplete.MetaStepdef;
import cucumber.runtime.autocomplete.StepdefGenerator;
import cucumber.runtime.converters.LocalizedXStreams;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberTagStatement;
import gherkin.formatter.Argument;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Comment;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Step;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static cucumber.runtime.model.CucumberFeature.load;
import static java.util.Collections.emptyList;

/**
 * This is the main entry point for running Cucumber features.
 */
public class Runtime implements UnreportedStepExecutor {

    private static final Object DUMMY_ARG = new Object();
    private static final byte ERRORS = 0x1;
    private static final List<Object> NO_FILTERS = emptyList();

    private final UndefinedStepsTracker tracker;
    private final List<Throwable> errors = new ArrayList<Throwable>();
    private final Collection<? extends Backend> backends;
    private final boolean isDryRun;
    private final ResourceLoader resourceLoader;
    private Glue glue;
    //This is a good thing to keep at Runtime, since it's expensive to create
    private final LocalizedXStreams localizedXStreams = new LocalizedXStreams();


    //TODO: These are really state machine variables, and I'm not sure the runtime is the best place for this state machine
    //They really should be created each time a scenario is run, not in here
    private boolean skipNextStep = false;
    private ScenarioResultImpl scenarioResult = null;


    public Runtime(List<String> gluePaths, ResourceLoader resourceLoader) {
        this(gluePaths, resourceLoader, false);
    }

    public Runtime(List<String> gluePaths, ResourceLoader resourceLoader, boolean isDryRun) {
        this(gluePaths, resourceLoader, loadBackends(resourceLoader), isDryRun);
    }

    public Runtime(List<String> gluePaths, ResourceLoader resourceLoader, Collection<? extends Backend> backends, boolean isDryRun) {
        this.backends = backends;
        this.resourceLoader = resourceLoader;
        this.isDryRun = isDryRun;
        this.tracker = new UndefinedStepsTracker(backends);

        this.glue = new RuntimeGlue();
        for (Backend backend : backends) {
            backend.loadGlue(glue, gluePaths);
            backend.setUnreportedStepExecutor(this);
        }
    }

    private static Collection<? extends Backend> loadBackends(ResourceLoader resourceLoader) {
        return new ClasspathResourceLoader().instantiateSubclasses(Backend.class, "cucumber/runtime", new Class[]{ResourceLoader.class}, new Object[]{resourceLoader});
    }

    public void addError(Throwable error) {
        errors.add(error);
    }

    /**
     * This is where the first entry happens.
     * Glue shouldn't be passed along, since it's Glue, we should somehow expose the right bits to the various stages
     * so that the appropriate calls can be made at the appropriate time.
     *
     * @param featurePaths
     * @param filters
     * @param formatter
     * @param reporter
     */
    public void run(List<String> featurePaths, final List<Object> filters, gherkin.formatter.Formatter formatter, Reporter reporter) {
        for (CucumberFeature cucumberFeature : load(resourceLoader, featurePaths, filters)) {
            run(cucumberFeature, formatter, reporter);
        }
    }

    /**
     * Runs an individual feature, not all the features
     *
     * @param cucumberFeature
     * @param formatter
     * @param reporter
     */
    public void run(CucumberFeature cucumberFeature, Formatter formatter, Reporter reporter) {

        //For each feature, we need to set up the backend

        formatter.uri(cucumberFeature.getUri());
        formatter.feature(cucumberFeature.getFeature());
        for (CucumberTagStatement cucumberTagStatement : cucumberFeature.getFeatureElements()) {
            //Run the scenario, it should handle before and after hooks
            cucumberTagStatement.run(formatter, reporter, this);
        }
        formatter.eof();
    }

    public void buildBackendWorlds() {
        for (Backend backend : backends) {
            backend.buildWorld();
        }
        tracker.reset();
        //TODO: this is the initial state of the state machine, it should not go here, but into something else
        skipNextStep = false;
        scenarioResult = new ScenarioResultImpl();
    }

    public void disposeBackendWorlds() {
        for (Backend backend : backends) {
            backend.disposeWorld();
        }
    }

    /**
     * This is the second entry to running features
     *
     * @param featurePaths
     * @param dotCucumber
     * @throws IOException
     */
    public void writeStepdefsJson(List<String> featurePaths, File dotCucumber) throws IOException {
        List<CucumberFeature> features = load(resourceLoader, featurePaths, NO_FILTERS);
        buildBackendWorlds();
        List<StepDefinition> stepDefs = glue.getStepDefinitions();
        List<MetaStepdef> metaStepdefs = new StepdefGenerator().generate(stepDefs, features);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(metaStepdefs);

        FileWriter metaJson = new FileWriter(new File(dotCucumber, "stepdefs.json"));
        metaJson.append(json);
        metaJson.close();
    }

    public boolean isDryRun() {
        return isDryRun;
    }

    public List<Throwable> getErrors() {
        return errors;
    }

    public byte exitStatus() {
        byte result = 0x0;
        if (!errors.isEmpty()) {
            result |= ERRORS;
        }
        return result;
    }


    public void storeStepKeyword(Step step, Locale locale) {
        tracker.storeStepKeyword(step, locale);
    }

    public void addUndefinedStep(Step step, Locale locale) {
        tracker.addUndefinedStep(step, locale);
    }

    public List<String> getSnippets() {
        return tracker.getSnippets();
    }

    public Glue getGlue() {
        return glue;
    }

    public void runBeforeHooks(Reporter reporter, Set<String> tags) {
        runHooks(glue.getBeforeHooks(), reporter, tags);
    }

    public void runAfterHooks(Reporter reporter, Set<String> tags) {
        runHooks(glue.getAfterHooks(), reporter, tags);
    }

    private void runHooks(List<HookDefinition> hooks, Reporter reporter, Set<String> tags) {
        for (HookDefinition hook : hooks) {
            runHookIfTagsMatch(hook, reporter, tags);
        }
    }

    private void runHookIfTagsMatch(HookDefinition hook, Reporter reporter, Set<String> tags) {
        if (hook.matches(tags)) {
            long start = System.nanoTime();
            try {
                hook.execute(scenarioResult);
            } catch (Throwable t) {
                skipNextStep = true;

                long duration = System.nanoTime() - start;
                Result result = new Result(Result.FAILED, duration, t, DUMMY_ARG);
                scenarioResult.add(result);
                reporter.result(result);
            }
        }
    }


    //TODO: Maybe this should go into the cucumber step execution model and it should return the result of that execution!
    @Override
    public void runUnreportedStep(String uri, Locale locale, String stepKeyword, String stepName, int line) throws Throwable {
        Step step = new Step(Collections.<Comment>emptyList(), stepKeyword, stepName, line, null, null);

        StepDefinitionMatch match = stepDefinitionMatch(uri, step, locale);
        if (match == null) {
            UndefinedStepException error = new UndefinedStepException(step);

            StackTraceElement[] originalTrace = error.getStackTrace();
            StackTraceElement[] newTrace = new StackTraceElement[originalTrace.length + 1];
            newTrace[0] = new StackTraceElement("✽", "StepDefinition", uri, line);
            System.arraycopy(originalTrace, 0, newTrace, 1, originalTrace.length);
            error.setStackTrace(newTrace);

            throw error;
        }
        match.runStep(locale);
    }


    //TODO: should refactor this up into the runtime.
    public void runStep(String uri, Step step, Reporter reporter, Locale locale) {
        StepDefinitionMatch match = stepDefinitionMatch(uri, step, locale);
        if (match != null) {
            reporter.match(match);
        } else {
            reporter.match(Match.UNDEFINED);
            reporter.result(Result.UNDEFINED);
            skipNextStep = true;
            return;
        }

        if (isDryRun()) {
            skipNextStep = true;
        }

        if (skipNextStep) {
            scenarioResult.add(Result.SKIPPED);
            reporter.result(Result.SKIPPED);
        } else {
            String status = Result.PASSED;
            Throwable error = null;
            long start = System.nanoTime();
            try {
                match.runStep(locale);
            } catch (Throwable t) {
                error = t;
                status = Result.FAILED;
                addError(t);
                skipNextStep = true;
            } finally {
                long duration = System.nanoTime() - start;
                Result result = new Result(status, duration, error, DUMMY_ARG);
                scenarioResult.add(result);
                reporter.result(result);
            }
        }
    }

    private StepDefinitionMatch stepDefinitionMatch(String uri, Step step, Locale locale) {
        List<StepDefinitionMatch> matches = stepDefinitionMatches(uri, step);
        try {
            if (matches.size() == 0) {
                addUndefinedStep(step, locale);
                return null;
            }
            if (matches.size() == 1) {
                return matches.get(0);
            } else {
                throw new AmbiguousStepDefinitionsException(matches);
            }
        } finally {
            storeStepKeyword(step, locale);
        }
    }

    private List<StepDefinitionMatch> stepDefinitionMatches(String uri, Step step) {
        List<StepDefinitionMatch> result = new ArrayList<StepDefinitionMatch>();
        for (StepDefinition stepDefinition : glue.getStepDefinitions()) {
            List<Argument> arguments = stepDefinition.matchedArguments(step);
            if (arguments != null) {
                result.add(new StepDefinitionMatch(arguments, stepDefinition, uri, step, localizedXStreams));
            }
        }
        return result;
    }

}
