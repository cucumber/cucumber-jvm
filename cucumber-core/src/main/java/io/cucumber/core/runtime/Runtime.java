package io.cucumber.core.runtime;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.eventbus.UuidGenerator;
import io.cucumber.core.feature.FeatureParser;
import io.cucumber.core.filter.Filters;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.order.PickleOrder;
import io.cucumber.core.plugin.PluginFactory;
import io.cucumber.core.plugin.Plugins;
import io.cucumber.core.resource.ClassLoaders;
import io.cucumber.plugin.Plugin;
import org.jspecify.annotations.Nullable;

import java.time.Clock;
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
import java.util.function.Predicate;
import java.util.function.Supplier;

import static io.cucumber.core.runtime.SynchronizedEventBus.synchronize;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * This is the main entry point for running Cucumber features from the CLI.
 */
@SuppressWarnings("JavaLangClash")
public final class Runtime {

    private static final Logger log = LoggerFactory.getLogger(Runtime.class);

    private final ExitStatus exitStatus;

    private final Predicate<Pickle> filter;
    private final int limit;
    private final FeatureSupplier featureSupplier;
    private final ExecutorService executor;
    private final PickleOrder pickleOrder;
    private final CucumberExecutionContext context;

    private Runtime(
            final ExitStatus exitStatus,
            final CucumberExecutionContext context,
            final Predicate<Pickle> filter,
            final int limit,
            final FeatureSupplier featureSupplier,
            final ExecutorService executor,
            final PickleOrder pickleOrder
    ) {
        this.filter = filter;
        this.context = context;
        this.limit = limit;
        this.featureSupplier = featureSupplier;
        this.executor = executor;
        this.exitStatus = exitStatus;
        this.pickleOrder = pickleOrder;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void run() {
        // Parse the features early. Don't proceed when there are lexer errors
        List<Feature> features = featureSupplier.get();
        context.runFeatures(() -> runFeatures(features));
    }

    private void runFeatures(List<Feature> features) {
        features.forEach(context::beforeFeature);
        List<Future<?>> executingPickles = features.stream()
                .flatMap(feature -> feature.getPickles().stream())
                .filter(filter)
                .collect(collectingAndThen(toList(),
                    list -> pickleOrder.orderPickles(list).stream()))
                .limit(limit > 0 ? limit : Integer.MAX_VALUE)
                .map(pickle -> executor.submit(executePickle(pickle)))
                .collect(toList());

        executor.shutdown();

        for (Future<?> executingPickle : executingPickles) {
            try {
                executingPickle.get();
            } catch (ExecutionException e) {
                log.error(e, () -> "Exception while executing pickle");
            } catch (InterruptedException e) {
                log.debug(e, () -> "Interrupted while executing pickle");
                executor.shutdownNow();
            }
        }
    }

    private Runnable executePickle(Pickle pickle) {
        return () -> context.runTestCase(runner -> runner.runPickle(pickle));
    }

    public byte exitStatus() {
        return exitStatus.exitStatus();
    }

    public static final class Builder {

        private @Nullable EventBus eventBus;
        private Supplier<ClassLoader> classLoader = ClassLoaders::getDefaultClassLoader;
        private RuntimeOptions runtimeOptions = RuntimeOptions.defaultOptions();
        private @Nullable BackendSupplier backendSupplier;
        private @Nullable ObjectFactorySupplier objectFactorySupplier;
        private @Nullable FeatureSupplier featureSupplier;
        private List<Plugin> additionalPlugins = emptyList();
        private @Nullable Supplier<UuidGenerator> uuidGeneratorSupplier;

        private Builder() {
        }

        public Builder withRuntimeOptions(RuntimeOptions runtimeOptions) {
            this.runtimeOptions = runtimeOptions;
            return this;
        }

        public Builder withClassLoader(Supplier<ClassLoader> classLoader) {
            this.classLoader = classLoader;
            return this;
        }

        public Builder withBackendSupplier(BackendSupplier backendSupplier) {
            this.backendSupplier = backendSupplier;
            return this;
        }

        public Builder withObjectFactorySupplier(ObjectFactorySupplier objectFactorySupplier) {
            this.objectFactorySupplier = objectFactorySupplier;
            return this;
        }

        public Builder withFeatureSupplier(FeatureSupplier featureSupplier) {
            this.featureSupplier = featureSupplier;
            return this;
        }

        public Builder withUuidGeneratorSupplier(Supplier<UuidGenerator> uuidGenerator) {
            this.uuidGeneratorSupplier = uuidGenerator;
            return this;
        }

        public Builder withAdditionalPlugins(Plugin... plugins) {
            this.additionalPlugins = Arrays.asList(plugins);
            return this;
        }

        public Builder withEventBus(EventBus eventBus) {
            this.eventBus = eventBus;
            return this;
        }

        public Runtime build() {
            EventBus eventBus = synchronize(createEventBus());
            ExitStatus exitStatus = createPluginsAndExitStatus(eventBus);
            RunnerSupplier runnerSupplier = createRunnerSupplier(eventBus);
            CucumberExecutionContext context = new CucumberExecutionContext(eventBus, exitStatus, runnerSupplier);
            Predicate<Pickle> filter = new Filters(runtimeOptions);
            int limit = runtimeOptions.getLimitCount();
            FeatureSupplier featureSupplier = createFeatureSupplier(eventBus);
            ExecutorService executor = createExecutorService();
            PickleOrder pickleOrder = runtimeOptions.getPickleOrder();
            return new Runtime(exitStatus, context, filter, limit, featureSupplier, executor, pickleOrder);
        }

        private ExitStatus createPluginsAndExitStatus(EventBus eventBus) {
            Plugins plugins = createPlugins();
            ExitStatus exitStatus = new ExitStatus(runtimeOptions);
            plugins.addPlugin(exitStatus);

            if (runtimeOptions.isMultiThreaded()) {
                plugins.setSerialEventBusOnEventListenerPlugins(eventBus);
            } else {
                plugins.setEventBusOnEventListenerPlugins(eventBus);
            }
            return exitStatus;
        }

        private RunnerSupplier createRunnerSupplier(EventBus eventBus) {
            ObjectFactorySupplier objectFactorySupplier = createObjectFactorySupplier();
            BackendSupplier backendSupplier = createBackendSupplier(objectFactorySupplier);
            return runtimeOptions.isMultiThreaded()
                    ? new ThreadLocalRunnerSupplier(runtimeOptions, eventBus, backendSupplier, objectFactorySupplier)
                    : new SingletonRunnerSupplier(runtimeOptions, eventBus, backendSupplier, objectFactorySupplier);
        }

        private ObjectFactorySupplier createObjectFactorySupplier() {
            if (this.objectFactorySupplier != null) {
                return objectFactorySupplier;
            }
            ObjectFactoryServiceLoader objectFactoryServiceLoader = new ObjectFactoryServiceLoader(classLoader,
                runtimeOptions);
            return runtimeOptions.isMultiThreaded()
                    ? new ThreadLocalObjectFactorySupplier(objectFactoryServiceLoader)
                    : new SingletonObjectFactorySupplier(objectFactoryServiceLoader);
        }

        private BackendSupplier createBackendSupplier(ObjectFactorySupplier objectFactorySupplier) {
            return this.backendSupplier != null
                    ? this.backendSupplier
                    : new BackendServiceLoader(this.classLoader, objectFactorySupplier);
        }

        private EventBus createEventBus() {
            if (this.eventBus != null) {
                return this.eventBus;
            }
            UuidGenerator uuidGenerator = createUuidGenerator();
            return new TimeServiceEventBus(Clock.systemUTC(), uuidGenerator);
        }

        private UuidGenerator createUuidGenerator() {
            if (uuidGeneratorSupplier != null) {
                return uuidGeneratorSupplier.get();
            } else {
                return new UuidGeneratorServiceLoader(classLoader, runtimeOptions).loadUuidGenerator();
            }
        }

        private FeatureSupplier createFeatureSupplier(EventBus eventBus) {
            if (this.featureSupplier != null) {
                return this.featureSupplier;
            }
            FeatureParser parser = new FeatureParser(eventBus::generateId);
            return new FeaturePathFeatureSupplier(classLoader, runtimeOptions, parser);
        }

        private ExecutorService createExecutorService() {
            return runtimeOptions.isMultiThreaded()
                    ? Executors.newFixedThreadPool(runtimeOptions.getThreads(), new CucumberThreadFactory())
                    : new SameThreadExecutorService();
        }

        private Plugins createPlugins() {
            Plugins plugins = new Plugins(new PluginFactory(), runtimeOptions);
            for (Plugin plugin : additionalPlugins) {
                plugins.addPlugin(plugin);
            }
            return plugins;
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
            // no-op
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
