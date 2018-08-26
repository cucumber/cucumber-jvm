package cucumber.runtime;

import cucumber.api.Plugin;
import cucumber.api.StepDefinitionReporter;
import cucumber.api.event.TestRunFinished;
import cucumber.api.event.TestRunStarted;
import cucumber.runner.EventBus;
import cucumber.runner.TimeService;
import cucumber.runner.TimeServiceEventBus;
import cucumber.runner.RunnerSupplier;
import cucumber.runner.SingletonRunnerSupplier;
import cucumber.runner.ThreadLocalRunnerSupplier;
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
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * This is the main entry point for running Cucumber features from the CLI.
 */
public class Runtime {

    private final ExitStatus exitStatus;

    private final RuntimeOptions runtimeOptions;

    private final RunnerSupplier runnerSupplier;
    private final Filters filters;
    private final EventBus bus;
    private final FeatureSupplier featureSupplier;
    private final Plugins plugins;
    private final ExecutorService executor;

    public Runtime(final Plugins plugins,
                   final RuntimeOptions runtimeOptions,
                   final EventBus bus,
                   final Filters filters,
                   final RunnerSupplier runnerSupplier,
                   final FeatureSupplier featureSupplier,
                   final ExecutorService executor) {

        this.plugins = plugins;
        this.runtimeOptions = runtimeOptions;
        this.filters = filters;
        this.bus = bus;
        this.runnerSupplier = runnerSupplier;
        this.featureSupplier = featureSupplier;
        this.executor = executor;
        this.exitStatus = new ExitStatus(runtimeOptions);
        exitStatus.setEventPublisher(bus);
    }

    public void run() {
        final List<CucumberFeature> features = featureSupplier.get();
        bus.send(new TestRunStarted(bus.getTime()));
        for (CucumberFeature feature : features) {
            feature.sendTestSourceRead(bus);
        }

        final StepDefinitionReporter stepDefinitionReporter = plugins.stepDefinitionReporter();
        runnerSupplier.get().reportStepDefinitions(stepDefinitionReporter);

        final FeatureCompiler compiler = new FeatureCompiler();
        for (CucumberFeature feature : features) {
            for (final PickleEvent pickleEvent : compiler.compileFeature(feature)) {
                if (filters.matchesFilters(pickleEvent)) {
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            runnerSupplier.get().runPickle(pickleEvent);
                        }
                    });
                }
            }
        }
        executor.shutdown();
        try {
            //noinspection StatementWithEmptyBody we wait, nothing else
            while (!executor.awaitTermination(1, TimeUnit.DAYS)) ;
        } catch (InterruptedException e) {
            throw new CucumberException(e);
        }

        bus.send(new TestRunFinished(bus.getTime()));
    }

    public byte exitStatus() {
        return exitStatus.exitStatus();
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

            final RunnerSupplier runnerSupplier = runtimeOptions.isMultiThreaded()
                ? new ThreadLocalRunnerSupplier(this.runtimeOptions, eventBus, backendSupplier)
                : new SingletonRunnerSupplier(this.runtimeOptions, eventBus, backendSupplier);

            final ExecutorService executor = runtimeOptions.isMultiThreaded()
                ? Executors.newFixedThreadPool(runtimeOptions.getThreads())
                : new SameThreadExecutorService();


            final FeatureLoader featureLoader = new FeatureLoader(resourceLoader);

            final FeatureSupplier featureSupplier = this.featureSupplier != null
                ? this.featureSupplier
                : new FeaturePathFeatureSupplier(featureLoader, this.runtimeOptions);

            final RerunFilters rerunFilters = new RerunFilters(this.runtimeOptions, featureLoader);
            final Filters filters = new Filters(this.runtimeOptions, rerunFilters);
            return new Runtime(plugins, this.runtimeOptions, eventBus, filters, runnerSupplier, featureSupplier, executor);
        }
    }

    private static final class SameThreadExecutorService extends AbstractExecutorService {

        @Override
        public void execute(Runnable command) {
            command.run();
        }

        @Override
        public void shutdown() {
            //no-op
        }

        @Override
        public List<Runnable> shutdownNow() {
            return Collections.emptyList();
        }

        @Override
        public boolean isShutdown() {
            return true;
        }

        @Override
        public boolean isTerminated() {
            return true;
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) {
            return true;
        }
    }
}
