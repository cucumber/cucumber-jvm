package cucumber.runtime;

import cucumber.api.StepDefinitionReporter;
import cucumber.api.event.TestRunFinished;
import cucumber.api.event.TestRunStarted;
import cucumber.runner.EventBus;
import cucumber.runner.Runner;
import cucumber.runtime.filter.Filters;
import cucumber.runtime.formatter.Plugins;
import cucumber.runtime.model.CucumberFeature;
import gherkin.events.PickleEvent;

import java.util.List;

/**
 * This is the main entry point for running Cucumber features from the CLI.
 */
public class Runtime {

    private final ExitStatus exitStatus = new ExitStatus();

    private final RuntimeOptions runtimeOptions;

    private final Runner runner;
    private final Filters filters;
    private final EventBus bus;
    private final FeatureCompiler compiler = new FeatureCompiler();
    private final FeatureSupplier featureSupplier;
    private final Plugins plugins;

    public Runtime(Plugins plugins,
                   RuntimeOptions runtimeOptions,
                   EventBus bus,
                   Filters filters,
                   RunnerSupplier runnerSupplier,
                   FeatureSupplier featureSupplier
    ) {

        this.plugins = plugins;
        this.runtimeOptions = runtimeOptions;
        this.filters = filters;
        this.bus = bus;
        this.runner = runnerSupplier.get();
        this.featureSupplier = featureSupplier;
        exitStatus.setEventPublisher(bus);
    }

    public void run() {
        List<CucumberFeature> features = featureSupplier.get();
        bus.send(new TestRunStarted(bus.getTime()));
        for (CucumberFeature feature : features) {
            feature.sendTestSourceRead(bus);
        }

        StepDefinitionReporter stepDefinitionReporter = plugins.stepDefinitionReporter();

        runner.reportStepDefinitions(stepDefinitionReporter);

        for (CucumberFeature cucumberFeature : features) {
            runFeature(cucumberFeature);
        }

        bus.send(new TestRunFinished(bus.getTime()));
    }

    private void runFeature(CucumberFeature feature) {
        for (PickleEvent pickleEvent : compiler.compileFeature(feature)) {
            if (filters.matchesFilters(pickleEvent)) {
                runner.runPickle(pickleEvent);
            }
        }
    }

    public byte exitStatus() {
        return exitStatus.exitStatus(runtimeOptions.isStrict());
    }

}
