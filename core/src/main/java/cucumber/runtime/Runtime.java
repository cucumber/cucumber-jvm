package cucumber.runtime;

import cucumber.api.Pending;
import cucumber.api.StepDefinitionReporter;
import cucumber.api.SummaryPrinter;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.xstream.LocalizedXStreams;
import gherkin.I18n;
import gherkin.formatter.Argument;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.*;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

/**
 * This is the main entry point for running Cucumber features.
 */
public class Runtime implements UnreportedStepExecutor {
    private static final String[] PENDING_EXCEPTIONS = new String[]{
            "org.junit.internal.AssumptionViolatedException"
    };

    static {
        Arrays.sort(PENDING_EXCEPTIONS);
    }

    private static final Object DUMMY_ARG = new Object();
    private static final byte ERRORS = 0x1;

    private final Stats stats;
    final UndefinedStepsTracker undefinedStepsTracker = new UndefinedStepsTracker();

    private final Glue glue;
    private final RuntimeOptions runtimeOptions;

    private final List<Throwable> errors = new ArrayList<Throwable>();
    private final Collection<? extends Backend> backends;
    private final ResourceLoader resourceLoader;
    private final ClassLoader classLoader;
    private final StopWatch stopWatch;

    //TODO: These are really state machine variables, and I'm not sure the runtime is the best place for this state machine
    //They really should be created each time a scenario is run, not in here
    private boolean skipNextStep = false;
    private ScenarioImpl scenarioResult = null;

    public Runtime(ResourceLoader resourceLoader, ClassFinder classFinder, ClassLoader classLoader, RuntimeOptions runtimeOptions) {
        this(resourceLoader, classLoader, loadBackends(resourceLoader, classFinder), runtimeOptions);
    }

    public Runtime(ResourceLoader resourceLoader, ClassLoader classLoader, Collection<? extends Backend> backends, RuntimeOptions runtimeOptions) {
        this(resourceLoader, classLoader, backends, runtimeOptions, StopWatch.SYSTEM, null);
    }

    public Runtime(ResourceLoader resourceLoader, ClassLoader classLoader, Collection<? extends Backend> backends,
                   RuntimeOptions runtimeOptions, RuntimeGlue optionalGlue) {
        this(resourceLoader, classLoader, backends, runtimeOptions, StopWatch.SYSTEM, optionalGlue);
    }

    public Runtime(ResourceLoader resourceLoader, ClassLoader classLoader, Collection<? extends Backend> backends,
                   RuntimeOptions runtimeOptions, StopWatch stopWatch, RuntimeGlue optionalGlue) {
        if (backends.isEmpty()) {
            throw new CucumberException("No backends were found. Please make sure you have a backend module on your CLASSPATH.");
        }
        this.resourceLoader = resourceLoader;
        this.classLoader = classLoader;
        this.backends = backends;
        this.runtimeOptions = runtimeOptions;
        this.stopWatch = stopWatch;
        this.glue = optionalGlue != null ? optionalGlue : new RuntimeGlue(undefinedStepsTracker, new LocalizedXStreams(classLoader));
        this.stats = new Stats(runtimeOptions.isMonochrome());

        for (Backend backend : backends) {
            backend.loadGlue(glue, runtimeOptions.getGlue());
            backend.setUnreportedStepExecutor(this);
        }
    }

    private static Collection<? extends Backend> loadBackends(ResourceLoader resourceLoader, ClassFinder classFinder) {
        Reflections reflections = new Reflections(classFinder);
        return reflections.instantiateSubclasses(Backend.class, "cucumber.runtime", new Class[]{ResourceLoader.class}, new Object[]{resourceLoader});
    }

    public void addError(Throwable error) {
        errors.add(error);
    }

    /**
     * This is the main entry point. Used from CLI, but not from JUnit.
     */
    public void run() throws IOException {
        // Make sure all features parse before initialising any reporters/formatters
        List<CucumberFeature> features = runtimeOptions.cucumberFeatures(resourceLoader);

        // TODO: This is duplicated in cucumber.api.android.CucumberInstrumentationCore - refactor or keep uptodate

        Formatter formatter = runtimeOptions.formatter(classLoader);
        Reporter reporter = runtimeOptions.reporter(classLoader);
        StepDefinitionReporter stepDefinitionReporter = runtimeOptions.stepDefinitionReporter(classLoader);

        glue.reportStepDefinitions(stepDefinitionReporter);

        for (CucumberFeature cucumberFeature : features) {
            cucumberFeature.run(formatter, reporter, this);
        }

        formatter.done();
        formatter.close();
        printSummary();
    }

    public void printSummary() {
        SummaryPrinter summaryPrinter = runtimeOptions.summaryPrinter(classLoader);
        summaryPrinter.print(this);
    }

    void printStats(PrintStream out) {
        stats.printStats(out, runtimeOptions.isStrict());
    }

    public void buildBackendWorlds(Reporter reporter, Set<Tag> tags, Scenario gherkinScenario) {
        for (Backend backend : backends) {
            backend.buildWorld();
        }
        undefinedStepsTracker.reset();
        //TODO: this is the initial state of the state machine, it should not go here, but into something else
        skipNextStep = false;
        scenarioResult = new ScenarioImpl(reporter, tags, gherkinScenario);
    }

    public void disposeBackendWorlds(String scenarioDesignation) {
        stats.addScenario(scenarioResult.getStatus(), scenarioDesignation);
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
        return runtimeOptions.isStrict() && hasUndefinedOrPendingSteps();
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
            if (!isPending(error)) {
                return true;
            }
        }
        return false;
    }

    public List<String> getSnippets() {
        return undefinedStepsTracker.getSnippets(backends, runtimeOptions.getSnippetType().getFunctionNameGenerator());
    }

    public Glue getGlue() {
        return glue;
    }

    public void runBeforeHooks(Reporter reporter, Set<Tag> tags) {
        runHooks(glue.getBeforeHooks(HookScope.SCENARIO), reporter, tags, true);
    }

    public void runAfterHooks(Reporter reporter, Set<Tag> tags) {
        runHooks(glue.getAfterHooks(HookScope.SCENARIO), reporter, tags, false);
    }

    public void runBeforeStepHooks(Reporter reporter, Step step) {
        runStepHooks(step, glue.getBeforeHooks(HookScope.STEP), reporter, true);
    }

    public void runAfterStepHooks(Reporter reporter, Step step) {
        runStepHooks(step, glue.getAfterHooks(HookScope.STEP), reporter, false);
    }

    private void runHooks(List<HookDefinition> hookDefinitions, Reporter reporter, Set<Tag> tags, boolean isBefore) {
        if (!runtimeOptions.isDryRun()) {
            for (HookDefinition hookDefinition : hookDefinitions) {
                if (hookDefinition.matches(tags)) {
                    runHook(scenarioResult, hookDefinition, reporter, isBefore);
                }
            }
        }
    }

    private void runStepHooks(Step step, List<HookDefinition> hookDefinitions, Reporter reporter, boolean isBefore) {
        if (!runtimeOptions.isDryRun()) {
            for (HookDefinition hookDefinition : hookDefinitions) {
                runHook(step, hookDefinition, reporter, isBefore);
            }
        }
    }

    private <T> void runHook(T arg, HookDefinition<T> hookDefinition, Reporter reporter, boolean isBefore) {
        String status = Result.PASSED;
        Throwable error = null;
        Match match = new Match(Collections.<Argument>emptyList(), hookDefinition.getLocation(false));
        stopWatch.start();
        try {
            hookDefinition.execute(arg);
        } catch (Throwable t) {
            error = t;
            status = isPending(t) ? "pending" : Result.FAILED;
            addError(t);
            skipNextStep = true;
        } finally {
            long duration = stopWatch.stop();
            Result result = new Result(status, duration, error, DUMMY_ARG);
            addHookToCounterAndResult(result);
            if (isBefore) {
                reporter.before(match, result);
            } else {
                reporter.after(match, result);
            }
        }
    }

    //TODO: Maybe this should go into the cucumber step execution model and it should return the result of that execution!
    @Override
    public void runUnreportedStep(String featurePath, I18n i18n, String stepKeyword, String stepName, int line, List<DataTableRow> dataTableRows, DocString docString) throws Throwable {
        Step step = new Step(Collections.<Comment>emptyList(), stepKeyword, stepName, line, dataTableRows, docString);

        StepDefinitionMatch match = glue.stepDefinitionMatch(featurePath, step, i18n);
        if (match == null) {
            UndefinedStepException error = new UndefinedStepException(step);

            StackTraceElement[] originalTrace = error.getStackTrace();
            StackTraceElement[] newTrace = new StackTraceElement[originalTrace.length + 1];
            newTrace[0] = new StackTraceElement("✽", "StepDefinition", featurePath, line);
            System.arraycopy(originalTrace, 0, newTrace, 1, originalTrace.length);
            error.setStackTrace(newTrace);

            throw error;
        }
        match.runStep(i18n);
    }

    public void runStep(String featurePath, Step step, Reporter reporter, I18n i18n) {
        StepDefinitionMatch match;

        try {
            match = glue.stepDefinitionMatch(featurePath, step, i18n);
        } catch (AmbiguousStepDefinitionsException e) {
            reporter.match(e.getMatches().get(0));
            Result result = new Result(Result.FAILED, 0L, e, DUMMY_ARG);
            reporter.result(result);
            addStepToCounterAndResult(result);
            addError(e);
            skipNextStep = true;
            return;
        }

        if (match != null) {
            reporter.match(match);
        } else {
            reporter.match(Match.UNDEFINED);
            reporter.result(Result.UNDEFINED);
            addStepToCounterAndResult(Result.UNDEFINED);
            skipNextStep = true;
            return;
        }

        if (runtimeOptions.isDryRun()) {
            skipNextStep = true;
        }

        if (skipNextStep) {
            addStepToCounterAndResult(Result.SKIPPED);
            reporter.result(Result.SKIPPED);
        } else {
            String status = Result.PASSED;
            Throwable error = null;
            stopWatch.start();
            try {
                match.runStep(i18n);
            } catch (Throwable t) {
                error = t;
                status = isPending(t) ? "pending" : Result.FAILED;
                addError(t);
                skipNextStep = true;
            } finally {
                long duration = stopWatch.stop();
                Result result = new Result(status, duration, error, DUMMY_ARG);
                addStepToCounterAndResult(result);
                reporter.result(result);
            }
        }
    }

    public static boolean isPending(Throwable t) {
        if (t == null) {
            return false;
        }
        return t.getClass().isAnnotationPresent(Pending.class) || Arrays.binarySearch(PENDING_EXCEPTIONS, t.getClass().getName()) >= 0;
    }

    private void addStepToCounterAndResult(Result result) {
        scenarioResult.add(result);
        stats.addStep(result);
    }

    private void addHookToCounterAndResult(Result result) {
        scenarioResult.add(result);
        stats.addHookTime(result.getDuration());
    }
}
