package io.cucumber.core.runtime;

import gherkin.events.PickleEvent;
import io.cucumber.core.api.event.ConcurrentEventListener;
import io.cucumber.core.api.event.EventHandler;
import io.cucumber.core.api.event.EventPublisher;
import io.cucumber.core.api.event.Result;
import io.cucumber.core.api.event.TestCaseFinished;
import io.cucumber.core.api.event.TestRunFinished;
import io.cucumber.core.api.event.TestRunStarted;
import io.cucumber.core.api.event.TestSourceRead;
import io.cucumber.core.api.plugin.Plugin;
import io.cucumber.core.api.plugin.StepDefinitionReporter;
import io.cucumber.core.backend.BackendSupplier;
import io.cucumber.core.backend.ObjectFactorySupplier;
import io.cucumber.core.backend.SingletonObjectFactorySupplier;
import io.cucumber.core.backend.ThreadLocalObjectFactorySupplier;
import io.cucumber.core.event.EventBus;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.filter.Filters;
import io.cucumber.core.io.ClassFinder;
import io.cucumber.core.io.MultiLoader;
import io.cucumber.core.io.ResourceLoader;
import io.cucumber.core.io.ResourceLoaderClassFinder;
import io.cucumber.core.model.CucumberFeature;
import io.cucumber.core.model.FeatureLoader;
import io.cucumber.core.options.Env;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.plugin.PluginFactory;
import io.cucumber.core.plugin.Plugins;
import io.cucumber.core.runner.TimeService;
import io.cucumber.core.runner.TimeServiceEventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static io.cucumber.core.api.event.Result.SEVERITY;
import static java.util.Collections.emptyList;
import static java.util.Collections.max;
import static java.util.Collections.min;

/**
 * This is the main entry point for running Cucumber features from the CLI.
 */
public final class Runtime {

    private final ExitStatus exitStatus;

    private final RunnerSupplier runnerSupplier;
    private final Filters filters;
    private final EventBus bus;
    private final FeatureSupplier featureSupplier;
    private final Plugins plugins;
    private final ExecutorService executor;

    private Runtime(final Plugins plugins,
                    final ExitStatus exitStatus,
                    final EventBus bus,
                    final Filters filters,
                    final RunnerSupplier runnerSupplier,
                    final FeatureSupplier featureSupplier,
                    final ExecutorService executor) {

        this.plugins = plugins;
        this.filters = filters;
        this.bus = bus;
        this.runnerSupplier = runnerSupplier;
        this.featureSupplier = featureSupplier;
        this.executor = executor;
        this.exitStatus = exitStatus;
    }

    public void run() {
        final List<CucumberFeature> features = featureSupplier.get();
        bus.send(new TestRunStarted(bus.getTime(), bus.getTimeMillis()));
        for (CucumberFeature feature : features) {
            bus.send(new TestSourceRead(bus.getTime(), bus.getTimeMillis(), feature.getUri().toString(), feature.getSource()));
        }
        final StepDefinitionReporter stepDefinitionReporter = plugins.stepDefinitionReporter();
        runnerSupplier.get().reportStepDefinitions(stepDefinitionReporter);

        for (CucumberFeature feature : features) {
            for (final PickleEvent pickleEvent : feature.getPickles()) {
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

        bus.send(new TestRunFinished(bus.getTime(), bus.getTimeMillis()));
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
        private RuntimeOptions runtimeOptions;
        private BackendSupplier backendSupplier;
        private ResourceLoader resourceLoader;
        private ClassFinder classFinder;
        private FeatureSupplier featureSupplier;
        private List<Plugin> additionalPlugins = emptyList();
        private List<String> runtimeOptionsArgs = emptyList();

        private Builder() {
        }

        public Builder withArgs(final String... args) {
            return withArgs(Arrays.asList(args));
        }

        public Builder withArgs(final List<String> args) {
            this.runtimeOptionsArgs = args;
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

            final RuntimeOptions runtimeOptions = this.runtimeOptions != null
                ? this.runtimeOptions
                : new RuntimeOptions(resourceLoader, Env.INSTANCE, runtimeOptionsArgs);

            final ClassFinder classFinder = this.classFinder != null
                ? this.classFinder
                : new ResourceLoaderClassFinder(resourceLoader, this.classLoader);

            final ObjectFactorySupplier objectFactorySupplier = runtimeOptions.isMultiThreaded()
                ? new ThreadLocalObjectFactorySupplier()
                : new SingletonObjectFactorySupplier();

            final BackendSupplier backendSupplier = this.backendSupplier != null
                ? this.backendSupplier
                : new BackendServiceLoader(resourceLoader, classFinder, runtimeOptions, objectFactorySupplier);

            final Plugins plugins = new Plugins(new PluginFactory(), this.eventBus, runtimeOptions);
            for (final Plugin plugin : additionalPlugins) {
                plugins.addPlugin(plugin);
            }
            final ExitStatus exitStatus = new ExitStatus(runtimeOptions);
            plugins.addPlugin(exitStatus);

            final RunnerSupplier runnerSupplier = runtimeOptions.isMultiThreaded()
                ? new ThreadLocalRunnerSupplier(runtimeOptions, eventBus, backendSupplier, objectFactorySupplier)
                : new SingletonRunnerSupplier(runtimeOptions, eventBus, backendSupplier, objectFactorySupplier);

            final ExecutorService executor = runtimeOptions.isMultiThreaded()
                ? Executors.newFixedThreadPool(runtimeOptions.getThreads(), new CucumberThreadFactory())
                : new SameThreadExecutorService();


            final FeatureLoader featureLoader = new FeatureLoader(resourceLoader);

            final FeatureSupplier featureSupplier = this.featureSupplier != null
                ? this.featureSupplier
                : new FeaturePathFeatureSupplier(featureLoader, runtimeOptions);

            final Filters filters = new Filters(runtimeOptions);

            return new Runtime(plugins, exitStatus, eventBus, filters, runnerSupplier, featureSupplier, executor);
        }
    }

    private static final class CucumberThreadFactory implements ThreadFactory {

        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        CucumberThreadFactory() {
            this.namePrefix = "cucumber-runner-" + poolNumber.getAndIncrement() + "-thread-";
        }
        
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, namePrefix + this.threadNumber.getAndIncrement());
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

    static final class ExitStatus implements ConcurrentEventListener {
        private static final byte DEFAULT = 0x0;
        private static final byte ERRORS = 0x1;

        private final List<Result> results = new ArrayList<>();
        private final RuntimeOptions runtimeOptions;

        private final EventHandler<TestCaseFinished> testCaseFinishedHandler = new EventHandler<TestCaseFinished>() {
            @Override
            public void receive(TestCaseFinished event) {
                results.add(event.result);
            }
        };

        ExitStatus(RuntimeOptions runtimeOptions) {
            this.runtimeOptions = runtimeOptions;
        }

        @Override
        public void setEventPublisher(EventPublisher publisher) {
            publisher.registerHandlerFor(TestCaseFinished.class, testCaseFinishedHandler);
        }

        byte exitStatus() {
            if (results.isEmpty()) {
                return DEFAULT;
            }

            if (runtimeOptions.isWip()) {
                return min(results, SEVERITY).is(Result.Type.PASSED) ? ERRORS : DEFAULT;
            }

            return max(results, SEVERITY).isOk(runtimeOptions.isStrict()) ? DEFAULT : ERRORS;
        }
    }
}
