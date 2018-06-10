package cucumber.runtime;

import cucumber.api.Plugin;
import cucumber.api.StepDefinitionReporter;
import cucumber.api.event.TestRunFinished;
import cucumber.api.event.TestRunStarted;
import cucumber.runner.EventBus;
import cucumber.runner.TimeService;
import cucumber.runner.TimeServiceEventBus;
import cucumber.runtime.filter.Filters;
import cucumber.runtime.filter.RerunFilters;
import cucumber.runtime.formatter.PluginFactory;
import cucumber.runtime.formatter.Plugins;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.FeatureLoader;
import gherkin.events.PickleEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

    public Runtime(final Plugins plugins,
            final RuntimeOptions runtimeOptions,
            final EventBus bus,
            final Filters filters,
            final RunnerSupplier runnerSupplier,
            final FeatureSupplier featureSupplier
    ) {

        this.plugins = plugins;
        this.runtimeOptions = runtimeOptions;
        this.filters = filters;
        this.bus = bus;
        this.runnerSupplier = runnerSupplier;
        this.featureSupplier = featureSupplier;
        exitStatus.setEventPublisher(bus);
    }

    public void run() throws InterruptedException {
        final List<CucumberFeature> features = featureSupplier.get();
        bus.send(new TestRunStarted(bus.getTime()));
        for (CucumberFeature feature : features) {
            feature.sendTestSourceRead(bus);
        }

        final StepDefinitionReporter stepDefinitionReporter = plugins.stepDefinitionReporter();
        runnerSupplier.get().reportStepDefinitions(stepDefinitionReporter);

        final ExecutorService executor = Executors.newFixedThreadPool(runtimeOptions.getThreads());
        final FeatureCompiler compiler = new FeatureCompiler();
        for (CucumberFeature feature : features) {
            for (final PickleEvent pickleEvent : compiler.compileFeature(feature)) {
                if (filters.matchesFilters(pickleEvent)) {
                    executor.submit(new Runnable() {
                        @Override
                        public void run() {
                            runnerSupplier.get().runPickle(pickleEvent);
                        }
                    });
                }
            }
        }
        executor.shutdown();

        do {
            executor.awaitTermination(1, TimeUnit.DAYS);
        } while (!executor.isShutdown());

        bus.send(new TestRunFinished(bus.getTime()));
    }

    public byte exitStatus() {
        return exitStatus.exitStatus(runtimeOptions.isStrict());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private EventBus eventBus = new TimeServiceEventBus(TimeService.SYSTEM);
        private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        private RuntimeOptions runtimeOptions = new RuntimeOptions("");
        private BackendSupplier backendSupplier;
        private ResourceLoader resourceLoader;
        private ClassFinder classFinder;
        private GlueSupplier glueSupplier = new RuntimeGlueSupplier();
        private FeatureSupplier featureSupplier;
        private List<Plugin> additionalPlugins = Collections.emptyList();

        private Builder() {
        }

        public Builder withArg(final String arg) {
            this.runtimeOptions = new RuntimeOptions(arg);
            return this;
        }

        public Builder withArgs(final String... args) {
            return withArgs(Arrays.asList(args));
        }

        public Builder withArgs(final List<String> args) {
            this.runtimeOptions = new RuntimeOptions(args);
            return this;
        }

        public Builder withRuntimeOptions(final RuntimeOptions runtimeOptions) {
            this.runtimeOptions = runtimeOptions;
            return this;
        }

        public Builder withClassLoader(final ClassLoader classLoader) {
            this.classLoader = classLoader;
            return this;
        }

        public Builder withResourceLoader(final ResourceLoader resourceLoader) {
            this.resourceLoader = resourceLoader;
            return this;
        }

        public Builder withClassFinder(final ClassFinder classFinder) {
            this.classFinder = classFinder;
            return this;
        }

        public Builder withBackendSupplier(final BackendSupplier backendSupplier) {
            this.backendSupplier = backendSupplier;
            return this;
        }

        public Builder withGlueSupplier(final GlueSupplier glueSupplier) {
            this.glueSupplier = glueSupplier;
            return this;
        }

        public Builder withFeatureSupplier(final FeatureSupplier featureSupplier) {
            this.featureSupplier = featureSupplier;
            return this;
        }

        public Builder withAdditionalPlugins(final Plugin... plugins) {
            this.additionalPlugins = Arrays.asList(plugins);
            return this;
        }

        public Builder withEventBus(final EventBus eventBus) {
            this.eventBus = eventBus;
            return this;
        }

        public Runtime build() {
            final ResourceLoader resourceLoader = this.resourceLoader != null
                ? this.resourceLoader
                : new MultiLoader(this.classLoader);

            final ClassFinder classFinder = this.classFinder != null
                ? this.classFinder
                : new ResourceLoaderClassFinder(resourceLoader, this.classLoader);

            final BackendSupplier backendSupplier = this.backendSupplier != null
                ? this.backendSupplier
                : new BackendModuleBackendSupplier(resourceLoader, classFinder, this.runtimeOptions);

            final Plugins plugins = new Plugins(this.classLoader, new PluginFactory(), this.eventBus, this.runtimeOptions);
            for (final Plugin plugin : additionalPlugins) {
                plugins.addPlugin(plugin);
            }

            final RunnerSupplier runnerSupplier = new ThreadLocalRunnerSupplier(this.runtimeOptions, this.eventBus, backendSupplier, this.glueSupplier);

            final FeatureLoader featureLoader = new FeatureLoader(resourceLoader);

            final FeatureSupplier featureSupplier = this.featureSupplier != null
                ? this.featureSupplier
                : new FeaturePathFeatureSupplier(featureLoader, this.runtimeOptions);

            final RerunFilters rerunFilters = new RerunFilters(this.runtimeOptions, featureLoader);
            final Filters filters = new Filters(this.runtimeOptions, rerunFilters);
            return new Runtime(plugins, this.runtimeOptions, this.eventBus, filters, runnerSupplier, featureSupplier);
        }
    }
}
