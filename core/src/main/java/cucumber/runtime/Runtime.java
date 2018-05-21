package cucumber.runtime;

import cucumber.api.StepDefinitionReporter;
import cucumber.api.event.TestRunFinished;
import cucumber.runner.EventBus;
import cucumber.runner.Runner;
import cucumber.runner.TimeService;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.model.CucumberFeature;
import gherkin.events.PickleEvent;
import gherkin.pickles.Compiler;
import gherkin.pickles.Pickle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * This is the main entry point for running Cucumber features.
 */
public class Runtime {

    private final ExitStatus exitStatus = new ExitStatus();

    private final RuntimeOptions runtimeOptions;

    private final ResourceLoader resourceLoader;
    private final ClassLoader classLoader;
    private final Runner runner;
    private final List<PicklePredicate> filters;
    private final EventBus bus;
    private final Compiler compiler = new Compiler();

    public Runtime(ResourceLoader resourceLoader,
                   ClassLoader classLoader,
                   Supplier<Collection<? extends Backend>> backendSupplier,
                   RuntimeOptions runtimeOptions,
                   TimeService stopWatch,
                   Supplier<Glue> glueSupplier) {

        this.resourceLoader = resourceLoader;
        this.classLoader = classLoader;
        this.runtimeOptions = runtimeOptions;
        this.bus = new EventBus(stopWatch);
        this.runner = createRunner(backendSupplier, runtimeOptions, glueSupplier);
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

        exitStatus.setEventPublisher(bus);
        runtimeOptions.setEventBus(bus);
    }

    private Runner createRunner(Supplier<Collection<? extends Backend>> backendSupplier, RuntimeOptions runtimeOptions, Supplier<Glue> glueSupplier) {
        Collection<? extends Backend> backends = backendSupplier.get();
        if (backends.isEmpty()) {
            throw new CucumberException("No backends were found. Please make sure you have a backend module on your CLASSPATH.");
        }
        return new Runner(glueSupplier.get(), bus, backends, runtimeOptions);
    }

    /**
     * This is the main entry point. Used from CLI, but not from JUnit.
     */
    public void run() {
        // Make sure all features parse before initialising any reporters/formatters
        List<CucumberFeature> features = runtimeOptions.cucumberFeatures(resourceLoader, bus);

        // TODO: This is duplicated in cucumber.api.android.CucumberInstrumentationCore - refactor or keep uptodate

        StepDefinitionReporter stepDefinitionReporter = runtimeOptions.stepDefinitionReporter(classLoader);

        reportStepDefinitions(stepDefinitionReporter);

        for (CucumberFeature cucumberFeature : features) {
            runFeature(cucumberFeature);
        }

        bus.send(new TestRunFinished(bus.getTime()));
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

    public byte exitStatus() {
        return exitStatus.exitStatus(runtimeOptions.isStrict());
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
