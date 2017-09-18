package cucumber.runtime;

import cucumber.api.StepDefinitionReporter;
import cucumber.api.SummaryPrinter;
import cucumber.api.event.TestRunFinished;
import cucumber.runner.EventBus;
import cucumber.runner.Runner;
import cucumber.runner.TimeService;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.xstream.LocalizedXStreams;
import cucumber.util.log.Logger;
import cucumber.util.log.LoggerFactory;
import gherkin.events.PickleEvent;
import gherkin.pickles.Compiler;
import gherkin.pickles.Pickle;
import org.omg.SendingContext.RunTime;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * This is the main entry point for running Cucumber features.
 */
public class Runtime {

    private static final Logger logger = LoggerFactory.getLogger(RunTime.class);

    final Stats stats; // package private to be avaiable for tests.
    private final UndefinedStepsTracker undefinedStepsTracker = new UndefinedStepsTracker();

    private final RuntimeOptions runtimeOptions;

    private final ResourceLoader resourceLoader;
    private final ClassLoader classLoader;
    private final Runner runner;
    private final List<PicklePredicate> filters;
    private final EventBus bus;
    private final Compiler compiler = new Compiler();
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
        final Glue glue;
        glue = optionalGlue == null ? new RuntimeGlue(new LocalizedXStreams(classLoader, runtimeOptions.getConverters())) : optionalGlue;
        this.stats = new Stats(runtimeOptions.isMonochrome());
        this.bus = new EventBus(stopWatch);
        this.runner = new Runner(glue, bus, backends, runtimeOptions);
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
        logger.info("Run started");
        // Make sure all features parse before initialising any reporters/formatters
        List<CucumberFeature> features = runtimeOptions.cucumberFeatures(resourceLoader, bus);

        // TODO: This is duplicated in cucumber.api.android.CucumberInstrumentationCore - refactor or keep uptodate

        StepDefinitionReporter stepDefinitionReporter = runtimeOptions.stepDefinitionReporter(classLoader);

        reportStepDefinitions(stepDefinitionReporter);

        for (CucumberFeature cucumberFeature : features) {
            runFeature(cucumberFeature);
        }

        bus.send(new TestRunFinished(bus.getTime()));
        printSummary();
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

    public Glue getGlue() {
        return runner.getGlue();
    }

    public EventBus getEventBus() {
        return bus;
    }

    public Runner getRunner() {
        return runner;
    }
}
