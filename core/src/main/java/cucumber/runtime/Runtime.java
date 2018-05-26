package cucumber.runtime;

import cucumber.api.StepDefinitionReporter;
import cucumber.api.event.TestRunFinished;
import cucumber.api.event.TestRunStarted;
import cucumber.runner.EventBus;
import cucumber.runner.Runner;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.FeatureLoader;
import gherkin.events.PickleEvent;

import java.util.List;

/**
 * This is the main entry point for running Cucumber features.
 */
public class Runtime {

    private final ExitStatus exitStatus = new ExitStatus();

    private final RuntimeOptions runtimeOptions;

    private final ClassLoader classLoader;
    private final Runner runner;
    private final Filters filters;
    private final EventBus bus;
    private final FeatureCompiler compiler = new FeatureCompiler();
    private final Supplier<List<CucumberFeature>> featureSupplier;


    public Runtime(ClassLoader classLoader,
                   RuntimeOptions runtimeOptions,
                   EventBus bus,
                   Filters filters,
                   Supplier<Runner> runnerSupplier,
                   Supplier<List<CucumberFeature>> featureSupplier
    ) {

        this.classLoader = classLoader;
        this.runtimeOptions = runtimeOptions;
        this.filters = filters;
        this.bus = bus;
        this.runner = runnerSupplier.get();
        this.featureSupplier = featureSupplier;
        exitStatus.setEventPublisher(bus);
    }

    /**
     * This is the main entry point. Used from CLI, but not from JUnit.
     */
    public void run() {
        List<CucumberFeature> features = featureSupplier.get();
        runtimeOptions.setEventBus(bus);
        runtimeOptions.getPlugins(); // to create the formatter objects
        bus.send(new TestRunStarted(bus.getTime()));
        for (CucumberFeature feature : features) {
            feature.sendTestSourceRead(bus);
        }

        StepDefinitionReporter stepDefinitionReporter = runtimeOptions.stepDefinitionReporter(classLoader);

        runner.reportStepDefinitions(stepDefinitionReporter);

        for (CucumberFeature cucumberFeature : features) {
            runFeature(cucumberFeature);
        }

        bus.send(new TestRunFinished(bus.getTime()));
    }

    void runFeature(CucumberFeature feature) {
        for (PickleEvent pickleEvent : compiler.compileFeature(feature)) {
            if (getFilters().matchesFilters(pickleEvent)) {
                runner.runPickle(pickleEvent);
            }
        }
    }

    public byte exitStatus() {
        return exitStatus.exitStatus(runtimeOptions.isStrict());
    }

    public Runner getRunner() {
        return runner;
    }

    public Filters getFilters() {
        return filters;
    }

}
