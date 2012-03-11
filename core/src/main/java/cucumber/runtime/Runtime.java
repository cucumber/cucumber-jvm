package cucumber.runtime;

import cucumber.io.ClasspathResourceLoader;
import cucumber.io.FileResourceLoader;
import cucumber.io.ResourceLoader;
import cucumber.runtime.converters.LocalizedXStreams;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberTagStatement;
import gherkin.I18n;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Comment;
import gherkin.formatter.model.DataTableRow;
import gherkin.formatter.model.DocString;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Step;
import gherkin.formatter.model.Tag;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static cucumber.runtime.model.CucumberFeature.load;

/**
 * This is the main entry point for running Cucumber features.
 */
public class Runtime implements UnreportedStepExecutor {

    private static final Object DUMMY_ARG = new Object();
    private static final byte ERRORS = 0x1;

    private final UndefinedStepsTracker undefinedStepsTracker = new UndefinedStepsTracker();
    private final Glue glue;

    private final List<Throwable> errors = new ArrayList<Throwable>();
    private final Collection<? extends Backend> backends;
    private final boolean isDryRun;
    private final ResourceLoader resourceLoader;

    //TODO: These are really state machine variables, and I'm not sure the runtime is the best place for this state machine
    //They really should be created each time a scenario is run, not in here
    private boolean skipNextStep = false;
    private ScenarioResultImpl scenarioResult = null;
    private final RuntimeOptions runtimeOptions;

    public Runtime(ResourceLoader resourceLoader, List<String> gluePaths, ClassLoader classLoader) {
        this(resourceLoader, gluePaths, classLoader, false);
    }

    public Runtime(ResourceLoader resourceLoader, List<String> gluePaths, ClassLoader classLoader, boolean isDryRun) {
        this(resourceLoader, gluePaths, classLoader, loadBackends(resourceLoader, classLoader), isDryRun, null);
    }

    public Runtime(ResourceLoader resourceLoader, List<String> gluePaths, ClassLoader classLoader, RuntimeOptions runtimeOptions) {
        this(resourceLoader, gluePaths, classLoader, loadBackends(resourceLoader, classLoader), runtimeOptions.dryRun, runtimeOptions);
    }

    public Runtime(ResourceLoader resourceLoader, List<String> gluePaths, ClassLoader classLoader, Collection<? extends Backend> backends, boolean isDryRun, RuntimeOptions runtimeOptions) {
        if (backends.isEmpty()) {
            throw new CucumberException("No backends were found. Please make sure you have a backend module on your CLASSPATH.");
        }
        this.backends = backends;
        this.resourceLoader = resourceLoader;
        this.isDryRun = isDryRun;
        glue = new RuntimeGlue(undefinedStepsTracker, new LocalizedXStreams(classLoader));

        for (Backend backend : backends) {
            backend.loadGlue(glue, gluePaths);
            backend.setUnreportedStepExecutor(this);
        }
        this.runtimeOptions = runtimeOptions;
    }

    private static Collection<? extends Backend> loadBackends(ResourceLoader resourceLoader, ClassLoader classLoader) {
        return new ClasspathResourceLoader(classLoader).instantiateSubclasses(Backend.class, "cucumber.runtime", new Class[]{ResourceLoader.class}, new Object[]{resourceLoader});
    }

    public void addError(Throwable error) {
        errors.add(error);
    }

    /**
     * This is the main entry point. Used from CLI, but not from JUnit.
     *
     * @param featurePaths
     * @param filters
     * @param formatter
     * @param reporter
     */
    public void run(List<String> featurePaths, final List<Object> filters, Formatter formatter, Reporter reporter) {
        for (CucumberFeature cucumberFeature : load(resourceLoader, featurePaths, filters)) {
            run(cucumberFeature, formatter, reporter);
        }
    }

    /**
     * Runs an individual feature, not all the features. Used from CLI, but not from JUnit.
     *
     * @param cucumberFeature
     * @param formatter
     * @param reporter
     */
    public void run(CucumberFeature cucumberFeature, Formatter formatter, Reporter reporter) {
        formatter.uri(cucumberFeature.getUri());
        formatter.feature(cucumberFeature.getFeature());
        for (CucumberTagStatement cucumberTagStatement : cucumberFeature.getFeatureElements()) {
            //Run the scenario, it should handle before and after hooks
            cucumberTagStatement.run(formatter, reporter, this);
        }
        formatter.eof();
    }

    public void buildBackendWorlds(Reporter reporter) {
        for (Backend backend : backends) {
            backend.buildWorld();
        }
        undefinedStepsTracker.reset();
        //TODO: this is the initial state of the state machine, it should not go here, but into something else
        skipNextStep = false;
        scenarioResult = new ScenarioResultImpl(reporter);
    }

    public void disposeBackendWorlds() {
        for (Backend backend : backends) {
            backend.disposeWorld();
        }
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

    public List<String> getSnippets() {
        return undefinedStepsTracker.getSnippets(backends);
    }

    public Glue getGlue() {
        return glue;
    }

    public void runBeforeHooks(Reporter reporter, Set<Tag> tags) {
        runHooks(glue.getBeforeHooks(), reporter, tags);
    }

    public void runAfterHooks(Reporter reporter, Set<Tag> tags) {
        runHooks(glue.getAfterHooks(), reporter, tags);
    }

    private void runHooks(List<HookDefinition> hooks, Reporter reporter, Set<Tag> tags) {
        for (HookDefinition hook : hooks) {
            runHookIfTagsMatch(hook, reporter, tags);
        }
    }

    private void runHookIfTagsMatch(HookDefinition hook, Reporter reporter, Set<Tag> tags) {
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
    public void runUnreportedStep(String uri, I18n i18n, String stepKeyword, String stepName, int line, List<DataTableRow> dataTableRows, DocString docString) throws Throwable {
        Step step = new Step(Collections.<Comment>emptyList(), stepKeyword, stepName, line, dataTableRows, docString);

        StepDefinitionMatch match = glue.stepDefinitionMatch(uri, step, i18n);
        if (match == null) {
            UndefinedStepException error = new UndefinedStepException(step);

            StackTraceElement[] originalTrace = error.getStackTrace();
            StackTraceElement[] newTrace = new StackTraceElement[originalTrace.length + 1];
            newTrace[0] = new StackTraceElement("✽", "StepDefinition", uri, line);
            System.arraycopy(originalTrace, 0, newTrace, 1, originalTrace.length);
            error.setStackTrace(newTrace);

            throw error;
        }
        match.runStep(i18n);
    }

    public void runStep(String uri, Step step, Reporter reporter, I18n i18n) {
        StepDefinitionMatch match;

        try {
            match = glue.stepDefinitionMatch(uri, step, i18n);
        } catch (AmbiguousStepDefinitionsException e) {
            reporter.match(e.getMatches().get(0));
            reporter.result(new Result(Result.FAILED, 0L, e, DUMMY_ARG));
            addError(e);
            skipNextStep = true;
            return;
        }

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
                match.runStep(i18n);
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

    public void writeStepdefsJson(List<String> featurePaths, File dotCucumber) throws IOException {
        glue.writeStepdefsJson(featurePaths, dotCucumber);
    }
}
