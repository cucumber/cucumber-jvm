package cucumber.runtime;

import cucumber.api.StepDefinitionReporter;
import cucumber.api.event.TestRunFinished;
import cucumber.api.event.TestRunStarted;
import cucumber.runner.EventBus;
import cucumber.runner.ParallelFeatureRunner;
import cucumber.runtime.filter.Filters;
import cucumber.runtime.formatter.Plugins;
import cucumber.runtime.model.CucumberFeature;

import java.util.List;

/**
 * This is the main entry point for running Cucumber features from the CLI.
 */
public class Runtime {

    private final ExitStatus exitStatus = new ExitStatus();

    private final RuntimeOptions runtimeOptions;

    private final RunnerSupplier runnerSupplier;
    private final Filters filters;
    private final EventBus bus;
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
        this.runnerSupplier = runnerSupplier;
        this.featureSupplier = featureSupplier;
        exitStatus.setEventPublisher(bus);
    }

    public void run() {
        List<CucumberFeature> features = featureSupplier.get();
        bus.send(new TestRunStarted(bus.getTime()));
        
        StepDefinitionReporter stepDefinitionReporter = plugins.stepDefinitionReporter();
        runnerSupplier.get().reportStepDefinitions(stepDefinitionReporter);
        
        final int requestedThreads = runtimeOptions.getThreads();
        if (!features.isEmpty()) {
            for (CucumberFeature feature : features) {
                feature.sendTestSourceRead(bus);
            }
            ParallelFeatureRunner.run(new FeatureRunner(filters, runnerSupplier), features, requestedThreads);
        }

        bus.send(new TestRunFinished(bus.getTime()));
    }

    public byte exitStatus() {
        return exitStatus.exitStatus(runtimeOptions.isStrict());
    }

}
