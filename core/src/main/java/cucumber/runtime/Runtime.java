package cucumber.runtime;

import cucumber.io.ClasspathResourceLoader;
import cucumber.io.ResourceLoader;
import cucumber.runtime.converters.LocalizedXStreams;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.snippets.SummaryPrinter;
import gherkin.I18n;
import gherkin.formatter.Argument;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.Comment;
import gherkin.formatter.model.DataTableRow;
import gherkin.formatter.model.DocString;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;
import gherkin.formatter.model.Step;
import gherkin.formatter.model.Tag;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * This is the main entry point for running Cucumber features.
 */
public class Runtime implements UnreportedStepExecutor {

    private static final Object DUMMY_ARG = new Object();
    private static final byte ERRORS = 0x1;

    final UndefinedStepsTracker undefinedStepsTracker = new UndefinedStepsTracker();

    private final Glue glue;
    private final RuntimeOptions runtimeOptions;

    private final List<Throwable> errors = new ArrayList<Throwable>();
    private final Collection<? extends Backend> backends;
    private final ResourceLoader resourceLoader;

    //TODO: These are really state machine variables, and I'm not sure the runtime is the best place for this state machine
    //They really should be created each time a scenario is run, not in here
    private boolean skipNextStep = false;
    private ScenarioResultImpl scenarioResult = null;
    private ClassLoader classLoader;

    public Runtime(ResourceLoader resourceLoader, ClassLoader classLoader, RuntimeOptions runtimeOptions) {
        this(resourceLoader, classLoader, loadBackends(resourceLoader, classLoader), runtimeOptions);
    }

    public Runtime(ResourceLoader resourceLoader, ClassLoader classLoader, Collection<? extends Backend> backends, RuntimeOptions runtimeOptions) {
        this.resourceLoader = resourceLoader;
        this.classLoader = classLoader;
        if (backends.isEmpty()) {
            throw new CucumberException("No backends were found. Please make sure you have a backend module on your CLASSPATH.");
        }
        this.backends = backends;
        glue = new RuntimeGlue(undefinedStepsTracker, new LocalizedXStreams(classLoader));

        for (Backend backend : backends) {
            backend.loadGlue(glue, runtimeOptions.glue);
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
     */
    public void run() {
        for (CucumberFeature cucumberFeature : runtimeOptions.cucumberFeatures(resourceLoader)) {
            run(cucumberFeature);
        }
        Formatter formatter = runtimeOptions.formatter(classLoader);

        formatter.done();
        printSummary();
        formatter.close();
    }

    private void run(CucumberFeature cucumberFeature) {
        Formatter formatter = runtimeOptions.formatter(classLoader);
        Reporter reporter = runtimeOptions.reporter(classLoader);
        cucumberFeature.run(formatter, reporter, this);
    }

    private void printSummary() {
        // TODO: inject a SummaryPrinter in the ctor
        new SummaryPrinter(System.out).print(this);
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

    public List<Throwable> getErrors() {
        return errors;
    }

    public byte exitStatus() {
        byte result = 0x0;
        if (hasErrors() || hasUndefinedOrPendingStepsAndIsStrict()) {
            result |= ERRORS;
        }
        return result;
    }

    private boolean hasUndefinedOrPendingStepsAndIsStrict() {
        return runtimeOptions.strict && hasUndefinedOrPendingSteps();
    }

    private boolean hasUndefinedOrPendingSteps() {
        return hasUndefinedSteps() || hasPendingSteps();
    }

    private boolean hasUndefinedSteps() {
        return undefinedStepsTracker.hasUndefinedSteps();
    }

    private boolean hasPendingSteps() {
        return !errors.isEmpty() && !hasErrors();
    }

    private boolean hasErrors() {
        for (Throwable error : errors) {
            if (!(error instanceof PendingException)) {
                return true;
            }
        }
        return false;
    }

    public List<String> getSnippets() {
        return undefinedStepsTracker.getSnippets(backends);
    }

    public Glue getGlue() {
        return glue;
    }

    public void runBeforeHooks(Reporter reporter, Set<Tag> tags) {
        runHooks(glue.getBeforeHooks(), reporter, tags, true);
    }

    public void runAfterHooks(Reporter reporter, Set<Tag> tags) {
        runHooks(glue.getAfterHooks(), reporter, tags, false);
    }

    private void runHooks(List<HookDefinition> hooks, Reporter reporter, Set<Tag> tags, boolean isBefore) {
        for (HookDefinition hook : hooks) {
            runHookIfTagsMatch(hook, reporter, tags, isBefore);
        }
    }

    private void runHookIfTagsMatch(HookDefinition hook, Reporter reporter, Set<Tag> tags, boolean isBefore) {
        if (hook.matches(tags)) {
            long start = System.nanoTime();
            try {
                hook.execute(scenarioResult);
            } catch (Throwable t) {
                skipNextStep = true;
                long duration = System.nanoTime() - start;

                Result result = new Result(Result.FAILED, duration, t, DUMMY_ARG);
                scenarioResult.add(result);
                addError(t);

                Match match = new Match(Collections.<Argument>emptyList(), hook.getLocation(false));
                if (isBefore) {
                    reporter.before(match, result);
                } else {
                    reporter.after(match, result);
                }
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

        if (runtimeOptions.dryRun) {
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
                status = (t instanceof PendingException) ? "pending" : Result.FAILED;
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

    public void writeStepdefsJson() throws IOException {
        glue.writeStepdefsJson(runtimeOptions.featurePaths, runtimeOptions.dotCucumber);
    }
}
