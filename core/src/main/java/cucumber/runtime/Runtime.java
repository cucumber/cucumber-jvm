package cucumber.runtime;

import cucumber.api.Plugin;
import cucumber.api.StepDefinitionReporter;
import cucumber.api.event.TestRunFinished;
import cucumber.api.event.TestRunStarted;
import cucumber.runner.EventBus;
import cucumber.runner.RunnerSupplier;
import cucumber.runner.SingletonRunnerSupplier;
import cucumber.runner.ThreadLocalRunnerSupplier;
import cucumber.runner.TimeService;
import cucumber.runner.TimeServiceEventBus;
import cucumber.runtime.filter.Filters;
import cucumber.runtime.formatter.PluginFactory;
import cucumber.runtime.formatter.Plugins;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.FeatureLoader;
import cucumber.runtime.order.OrderType;
import gherkin.events.PickleEvent;
import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is the main entry point for running Cucumber features from the CLI.
 */
public class Runtime {

    private static final Logger log = LoggerFactory.getLogger(Runtime.class);

    private final ExitStatus exitStatus;

    private final RuntimeOptions runtimeOptions;

    private final RunnerSupplier runnerSupplier;
    private final Filters filters;
    private final EventBus bus;
    private final FeatureSupplier featureSupplier;
    private final Plugins plugins;
    private final ExecutorService executor;
    private final OrderType orderType;

    public Runtime(final Plugins plugins,
                   final RuntimeOptions runtimeOptions,
                   final EventBus bus,
                   final Filters filters,
                   final RunnerSupplier runnerSupplier,
                   final FeatureSupplier featureSupplier,
                   final ExecutorService executor,
                   final OrderType orderType) {

        this.plugins = plugins;
        this.runtimeOptions = runtimeOptions;
        this.filters = filters;
        this.bus = bus;
        this.runnerSupplier = runnerSupplier;
        this.featureSupplier = featureSupplier;
        this.executor = executor;
        this.exitStatus = new ExitStatus(runtimeOptions);
        this.orderType = orderType;
        exitStatus.setEventPublisher(bus);
    }

    public void run() {
        final List<CucumberFeature> features = featureSupplier.get();
        bus.send(new TestRunStarted(bus.getTime(), bus.getTimeMillis()));
        for (CucumberFeature feature : features) {
            feature.sendTestSourceRead(bus);
        }

        final StepDefinitionReporter stepDefinitionReporter = plugins.stepDefinitionReporter();
        runnerSupplier.get().reportStepDefinitions(stepDefinitionReporter);
        
        final List<PickleEvent> filteredEvents = new ArrayList<>();
        for (CucumberFeature feature : features) {
            for (final PickleEvent pickleEvent : feature.getPickles()) {
                if (filters.matchesFilters(pickleEvent)) {
                	filteredEvents.add(pickleEvent);
                }
            }
        }
        
        final List<PickleEvent> orderedEvents = orderType.orderPickleEvents(filteredEvents);
        final List<PickleEvent> limitedEvents = filters.limitPickleEvents(orderedEvents);

         final List<Future<?>> executingPickles = new ArrayList<>();
        for(final PickleEvent pickleEvent : limitedEvents) {        	
        	executingPickles.add(executor.submit(new Runnable() {
                @Override
                public void run() {
                    runnerSupplier.get().runPickle(pickleEvent);
                }
            }));
        }

        executor.shutdown();

        List<Throwable> thrown = new ArrayList<>();
        for (Future executingPickle : executingPickles) {
            try {
                executingPickle.get();
            } catch (ExecutionException e){
                log.error("Exception while executing pickle", e);
                thrown.add(e.getCause());
            } catch (InterruptedException e){
                executor.shutdownNow();
                throw new CucumberException(e);
            }
        }
        if(thrown.size() == 1){
            throw new CucumberException(thrown.get(0));
        } else if (thrown.size() > 1){
            throw new CompositeCucumberException(thrown);
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
                ? Executors.newFixedThreadPool(runtimeOptions.getThreads(), new CucumberThreadFactory())
                : new SameThreadExecutorService();


            final FeatureLoader featureLoader = new FeatureLoader(resourceLoader);

            final FeatureSupplier featureSupplier = this.featureSupplier != null
                ? this.featureSupplier
                : new FeaturePathFeatureSupplier(featureLoader, this.runtimeOptions);

            final Filters filters = new Filters(this.runtimeOptions);
            final OrderType orderType = runtimeOptions.getOrderType();
            return new Runtime(plugins, this.runtimeOptions, eventBus, filters, runnerSupplier, featureSupplier, executor, orderType);
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
}
