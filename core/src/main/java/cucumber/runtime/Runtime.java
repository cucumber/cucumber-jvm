package cucumber.runtime;

import cucumber.api.StepDefinitionReporter;
import cucumber.api.SummaryPrinter;
import cucumber.api.event.TestGroupRunFinished;
import cucumber.api.event.TestGroupRunStarted;
import cucumber.api.event.TestRunFinished;
import cucumber.runner.DefaultUnreportedStepExecutor;
import cucumber.runner.EventBus;
import cucumber.runner.Runner;
import cucumber.runner.TimeService;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.xstream.LocalizedXStreams;
import gherkin.events.PickleEvent;
import gherkin.pickles.Compiler;
import gherkin.pickles.Pickle;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;

/**
 * This is the main entry point for running Cucumber features.
 */
public class Runtime {

    static final String SYNCHRONIZED_TAG = "@synchronized";
    static final String NOT_SYNCHRONIZED_TAG = "~" + SYNCHRONIZED_TAG;
    
    final Stats stats; // package private to be available for tests.
    private final UndefinedStepsTracker undefinedStepsTracker = new UndefinedStepsTracker();

    private final RuntimeOptions runtimeOptions;

    private final ResourceLoader resourceLoader;
    private final ClassLoader classLoader;
    private final Runner runner;
    private final List<PicklePredicate> filters;
    private final EventBus bus;
    private final Compiler compiler = new Compiler();
    private final Glue templateGlue;
    
    public Runtime(ResourceLoader resourceLoader, ClassFinder classFinder, ClassLoader classLoader, RuntimeOptions runtimeOptions) {
        this(resourceLoader, classLoader, loadBackends(resourceLoader, classFinder), runtimeOptions);
    }

    public Runtime(ResourceLoader resourceLoader, ClassLoader classLoader, Collection<? extends Backend> backends, RuntimeOptions runtimeOptions) {
        this(resourceLoader, classLoader, backends, runtimeOptions, TimeService.SYSTEM, null);
    }

    public Runtime(ResourceLoader resourceLoader, ClassLoader classLoader, Collection<? extends Backend> backends,
                   RuntimeOptions runtimeOptions, Glue optionalGlue) {
        this(resourceLoader, classLoader, backends, runtimeOptions, TimeService.SYSTEM, optionalGlue);
    }

    public Runtime(ResourceLoader resourceLoader, ClassLoader classLoader, Collection<? extends Backend> backends,
                   RuntimeOptions runtimeOptions, TimeService stopWatch, Glue optionalGlue) {
        if (backends.isEmpty()) {
            throw new CucumberException("No backends were found. Please make sure you have a backend module on your CLASSPATH.");
        }
        this.resourceLoader = resourceLoader;
        this.classLoader = classLoader;
        this.runtimeOptions = runtimeOptions;
        this.templateGlue = optionalGlue == null ? new RuntimeGlue(new LocalizedXStreams(classLoader, runtimeOptions.getConverters())) : optionalGlue;
        this.stats = new Stats(runtimeOptions.isMonochrome());
        this.bus = new EventBus(stopWatch);

        final UnreportedStepExecutor unreportedStepExecutor = new DefaultUnreportedStepExecutor(templateGlue);
        for (Backend backend : backends) {
            backend.loadGlue(templateGlue, runtimeOptions.getGlue());
            backend.setUnreportedStepExecutor(unreportedStepExecutor);
        }
        
        this.runner = new Runner(templateGlue, bus, backends, runtimeOptions);
        this.filters = new ArrayList<PicklePredicate>();
        List<String> tagFilters = runtimeOptions.getTagFilters();
        if (!tagFilters.isEmpty()) {
            this.filters.add(new TagPredicate(tagFilters));
        }
        List<Pattern> nameFilters = runtimeOptions.getNameFilters();
        if (!nameFilters.isEmpty()) {
            this.filters.add(new NamePredicate(nameFilters));
        }
        Map<String, List<Long>> lineFilters = runtimeOptions.getLineFilters(resourceLoader);
        if (!lineFilters.isEmpty()) {
            this.filters.add(new LinePredicate(lineFilters));
        }

        stats.setEventPublisher(bus);
        undefinedStepsTracker.setEventPublisher(bus);
        runtimeOptions.setEventBus(bus);
    }

    private static Collection<? extends Backend> loadBackends(ResourceLoader resourceLoader, ClassFinder classFinder) {
        Reflections reflections = new Reflections(classFinder);
        return reflections.instantiateSubclasses(Backend.class, "cucumber.runtime", new Class[]{ResourceLoader.class}, new Object[]{resourceLoader});
    }

    /**
     * This is the main entry point. Used from CLI, but not from JUnit.
     */
    public void run() throws IOException {
        final List<CucumberFeature> allFeatures = runtimeOptions.cucumberFeatures(resourceLoader, bus);
        
        // TODO: This is duplicated in cucumber.api.android.CucumberInstrumentationCore - refactor or keep uptodate
        StepDefinitionReporter stepDefinitionReporter = runtimeOptions.stepDefinitionReporter(classLoader);
        reportStepDefinitions(stepDefinitionReporter);
        
        final int requestedThreads = runtimeOptions.getThreads();
        
        final Map<String, Queue<CucumberFeature>> synchronisedFeatures = new FeatureFilter(SYNCHRONIZED_TAG).filterAndGroupBy(allFeatures);
        if (!synchronisedFeatures.isEmpty()) {
            int featureCount = 0;
            for(final Queue<CucumberFeature> featureGroup : synchronisedFeatures.values()) {
                featureCount+= featureGroup.size();
                allFeatures.removeAll(featureGroup);
            }
            runSynchronizedTests(featureCount, requestedThreads, synchronisedFeatures);
        }      
        
        if (!allFeatures.isEmpty()) {
            runRemainingTests(allFeatures, requestedThreads);
        }
        
        bus.send(new TestRunFinished(bus.getTime()));
        printSummary();
    }

    private void runSynchronizedTests(final int featureCount, final int requestedThreads, final Map<String, Queue<CucumberFeature>> synchronisedFeatures) {
        final ConcurrentLinkedQueue<Queue<CucumberFeature>> queuedFeatureGroups = new ConcurrentLinkedQueue<Queue<CucumberFeature>>(synchronisedFeatures.values());
        final int threadCount = Math.min(requestedThreads, synchronisedFeatures.size());
        final List<RuntimeCallable> tasks = new ArrayList<RuntimeCallable>(threadCount);
        for (int i = 0; i < threadCount; i++) {
            tasks.add(new RuntimeCallableFeatureGroupQueue(this, queuedFeatureGroups));
        }
        bus.send(new TestGroupRunStarted(SYNCHRONIZED_TAG, threadCount, featureCount, bus.getTime()));
        RuntimeCallableRunner.run(tasks);
        bus.send(new TestGroupRunFinished(SYNCHRONIZED_TAG, bus.getTime()));
    }

    private void runRemainingTests(final List<CucumberFeature> allFeatures, final int requestedThreads) {
        final ConcurrentLinkedQueue<CucumberFeature> queuedFeatures = new ConcurrentLinkedQueue<CucumberFeature>(allFeatures);
        final int threadCount = Math.min(requestedThreads, allFeatures.size());
        final List<RuntimeCallable> tasks = new ArrayList<RuntimeCallable>(threadCount);
        for (int i = 0; i < threadCount; i++) {
            tasks.add(new RuntimeCallableFeatureQueue(this, queuedFeatures));
        }
        bus.send(new TestGroupRunStarted(NOT_SYNCHRONIZED_TAG, threadCount, queuedFeatures.size(), bus.getTime()));
        RuntimeCallableRunner.run(tasks);
        bus.send(new TestGroupRunFinished(NOT_SYNCHRONIZED_TAG, bus.getTime()));
    }

    public void reportStepDefinitions(StepDefinitionReporter stepDefinitionReporter) {
        runner.reportStepDefinitions(stepDefinitionReporter);
    }
    
    public void runFeature(CucumberFeature feature) {
        List<PickleEvent> pickleEvents = compileFeature(feature);
        for (PickleEvent pickleEvent : pickleEvents) {
            if (matchesFilters(pickleEvent)) {
                runner.runPickle(pickleEvent);
            }
        }
    }

    public List<PickleEvent> compileFeature(CucumberFeature feature) {
        List<PickleEvent> pickleEvents = new ArrayList<PickleEvent>();
        for (Pickle pickle : compiler.compile(feature.getGherkinFeature())) {
            pickleEvents.add(new PickleEvent(feature.getUri(), pickle));
        }
        return pickleEvents;
    }

    public boolean matchesFilters(PickleEvent pickleEvent) {
        for (PicklePredicate filter : filters) {
            if (!filter.apply(pickleEvent)) {
                return false;
            }
        }
        return true;
    }

    public void printSummary() {
        SummaryPrinter summaryPrinter = runtimeOptions.summaryPrinter(classLoader);
        summaryPrinter.print(this);
    }

    void printStats(PrintStream out) {
        stats.printStats(out, runtimeOptions.isStrict());
    }

    public List<Throwable> getErrors() {
        return stats.getErrors();
    }

    public byte exitStatus() {
        return stats.exitStatus(runtimeOptions.isStrict());
    }

    public List<String> getSnippets() {
        return undefinedStepsTracker.getSnippets();
    }

    /**
     * Only use for test methods! 
     * Template Glue is returned, which is not necessarily all of the Glue used due the execution of tests
     */
    public Glue getGlue() {
        return templateGlue;
    }

    public EventBus getEventBus() {
        return bus;
    }

    public Runner getRunner() {
        return runner;
    }
}
