package cucumber.runtime;

import cucumber.api.StepDefinitionReporter;
import cucumber.api.event.TestRunFinished;
import cucumber.api.event.TestRunStarted;
import cucumber.runner.EventBus;
import cucumber.runner.Runner;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.model.CucumberFeature;
import gherkin.events.PickleEvent;
import java.util.List;

import static cucumber.runtime.model.CucumberFeature.load;

/**
 * This is the main entry point for running Cucumber features.
 */
public class Runtime {

    private final ExitStatus exitStatus = new ExitStatus();

    private final RuntimeOptions runtimeOptions;

    private final ResourceLoader resourceLoader;
    private final ClassLoader classLoader;
    private final Runner runner;
    private final Filters filters;
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
        final Glue glue;
//        glue = optionalGlue == null ? new RuntimeGlue() : optionalGlue;
        this.filters = new Filters(runtimeOptions, new RerunFilters(runtimeOptions, resourceLoader));
        this.bus = bus;
        this.runner = runnerSupplier.get();
        exitStatus.setEventPublisher(bus);
        runtimeOptions.setEventBus(bus);
    }

    /**
     * This is the main entry point. Used from CLI, but not from JUnit.
     */
    public void run() {
        List<CucumberFeature> features = load(resourceLoader, runtimeOptions.getFeaturePaths(), System.out);
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
