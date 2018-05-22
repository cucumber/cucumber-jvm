package cucumber.runtime;

import cucumber.api.StepDefinitionReporter;
import cucumber.api.event.TestRunFinished;
import cucumber.api.event.TestRunStarted;
import cucumber.runner.EventBus;
import cucumber.runner.Runner;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.model.CucumberFeature;
import gherkin.events.PickleEvent;

import java.util.ArrayList;
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
    private final FeatureCompiler compiler = new FeatureCompiler();


    public Runtime(ResourceLoader resourceLoader,
                   ClassLoader classLoader,
                   RuntimeOptions runtimeOptions,
                   EventBus bus,
                   Supplier<Runner> runnerSupplier
    ) {

        this.resourceLoader = resourceLoader;
        this.classLoader = classLoader;
        this.runtimeOptions = runtimeOptions;
        this.bus = bus;
        this.runner = runnerSupplier.get();
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

    /**
     * This is the main entry point. Used from CLI, but not from JUnit.
     */
    public void run() {
        bus.send(new TestRunStarted(bus.getTime()));

        // Make sure all features parse before initialising any reporters/formatters
        List<CucumberFeature> features = runtimeOptions.cucumberFeatures(resourceLoader, bus);

        StepDefinitionReporter stepDefinitionReporter = runtimeOptions.stepDefinitionReporter(classLoader);

        runner.reportStepDefinitions(stepDefinitionReporter);

        for (CucumberFeature cucumberFeature : features) {
            runFeature(cucumberFeature);
        }

        bus.send(new TestRunFinished(bus.getTime()));
    }

    void runFeature(CucumberFeature feature) {
        for (PickleEvent pickleEvent : compiler.compileFeature(feature)) {
            if (matchesFilters(pickleEvent)) {
                runner.runPickle(pickleEvent);
            }
        }
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

    public Runner getRunner() {
        return runner;
    }
}
