package io.cucumber.core.runtime;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.exception.CompositeCucumberException;
import io.cucumber.core.exception.CucumberException;
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
import io.cucumber.messages.Messages;
import io.cucumber.plugin.Plugin;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import io.cucumber.plugin.event.TestSourceRead;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
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

import static io.cucumber.messages.TimeConversion.javaInstantToTimestamp;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * This is the main entry point for running Cucumber features from the CLI.
 */
public final class Runtime {

    private static final Logger log = LoggerFactory.getLogger(Runtime.class);

    private final ExitStatus exitStatus;

    private final RunnerSupplier runnerSupplier;
    private final Predicate<Pickle> filter;
    private final int limit;
    private final EventBus bus;
    private final FeatureSupplier featureSupplier;
    private final ExecutorService executor;
    private final PickleOrder pickleOrder;

    private Runtime(final ExitStatus exitStatus,
                    final EventBus bus,
                    final Predicate<Pickle> filter,
                    final int limit,
                    final RunnerSupplier runnerSupplier,
                    final FeatureSupplier featureSupplier,
                    final ExecutorService executor,
                    final PickleOrder pickleOrder) {
        this.bus = bus;
        this.filter = filter;
        this.limit = limit;
        this.runnerSupplier = runnerSupplier;
        this.featureSupplier = featureSupplier;
        this.executor = executor;
        this.exitStatus = exitStatus;
        this.pickleOrder = pickleOrder;
    }

    public void run() {
        final List<Feature> features = featureSupplier.get();
        emitTestRunStarted();
        for (Feature feature : features) {
            emitTestSource(feature);
        }

        final List<Future<?>> executingPickles = features.stream()
            .flatMap(feature -> feature.getPickles().stream())
            .filter(filter)
            .collect(collectingAndThen(toList(),
                list -> pickleOrder.orderPickles(list).stream()))
            .limit(limit > 0 ? limit : Integer.MAX_VALUE)
            .map(pickle -> executor.submit(() -> runnerSupplier.get().runPickle(pickle)))
            .collect(toList());

        executor.shutdown();

        List<Throwable> thrown = new ArrayList<>();
        for (Future<?> executingPickle : executingPickles) {
            try {
                executingPickle.get();
            } catch (ExecutionException e) {
                log.error(e, () -> "Exception while executing pickle");
                thrown.add(e.getCause());
            } catch (InterruptedException e) {
                executor.shutdownNow();
                throw new CucumberException(e);
            }
        }
        if(thrown.isEmpty()){
            emitTestRunFinished(null);
        } else if (thrown.size() == 1) {
            CucumberException cucumberException = new CucumberException(thrown.get(0));
            emitTestRunFinished(cucumberException);
            throw cucumberException;
        } else {
            CompositeCucumberException compositeCucumberException = new CompositeCucumberException(thrown);
            emitTestRunFinished(compositeCucumberException);
            throw compositeCucumberException;
        }
    }

    private void emitTestRunStarted() {
        Instant instant = bus.getInstant();
        bus.send(new TestRunStarted(instant));
        bus.send(Messages.Envelope.newBuilder()
            .setTestRunStarted(Messages.TestRunStarted.newBuilder()
                .setTimestamp(javaInstantToTimestamp(instant)))
            .build());
    }

    private void emitTestSource(Feature feature) {
        bus.send(new TestSourceRead(bus.getInstant(), feature.getUri(), feature.getSource()));
        bus.sendAll(feature.getParseEvents());
    }

    private void emitTestRunFinished(CucumberException cucumberException) {
        Instant instant = bus.getInstant();
        bus.send(new TestRunFinished(instant));

        Messages.TestRunFinished.Builder testRunFinished = Messages.TestRunFinished.newBuilder()
            .setSuccess(exitStatus.isSuccess())
            .setTimestamp(javaInstantToTimestamp(instant));

        if (cucumberException != null) {
            testRunFinished.setMessage(cucumberException.getMessage());
        }
        bus.send(Messages.Envelope.newBuilder()
            .setTestRunFinished(testRunFinished)
            .build());
    }

    public byte exitStatus() {
        return exitStatus.exitStatus();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private EventBus eventBus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);
        private Supplier<ClassLoader> classLoader = ClassLoaders::getDefaultClassLoader;
        private RuntimeOptions runtimeOptions = RuntimeOptions.defaultOptions();
        private BackendSupplier backendSupplier;
        private FeatureSupplier featureSupplier;
        private List<Plugin> additionalPlugins = emptyList();

        private Builder() {
        }

        public Builder withRuntimeOptions(final RuntimeOptions runtimeOptions) {
            this.runtimeOptions = runtimeOptions;
            return this;
        }

        public Builder withClassLoader(final Supplier<ClassLoader> classLoader) {
            this.classLoader = classLoader;
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
            final ObjectFactoryServiceLoader objectFactoryServiceLoader = new ObjectFactoryServiceLoader(runtimeOptions);

            final ObjectFactorySupplier objectFactorySupplier = runtimeOptions.isMultiThreaded()
                ? new ThreadLocalObjectFactorySupplier(objectFactoryServiceLoader)
                : new SingletonObjectFactorySupplier(objectFactoryServiceLoader);

            final BackendSupplier backendSupplier = this.backendSupplier != null
                ? this.backendSupplier
                : new BackendServiceLoader(this.classLoader, objectFactorySupplier);

            final Plugins plugins = new Plugins(new PluginFactory(), runtimeOptions);
            for (final Plugin plugin : additionalPlugins) {
                plugins.addPlugin(plugin);
            }
            final ExitStatus exitStatus = new ExitStatus(runtimeOptions);
            plugins.addPlugin(exitStatus);
            if (runtimeOptions.isMultiThreaded()) {
                plugins.setSerialEventBusOnEventListenerPlugins(eventBus);
            } else {
                plugins.setEventBusOnEventListenerPlugins(eventBus);
            }

            final TypeRegistryConfigurerSupplier typeRegistryConfigurerSupplier = new ScanningTypeRegistryConfigurerSupplier(classLoader, runtimeOptions);

            final RunnerSupplier runnerSupplier = runtimeOptions.isMultiThreaded()
                ? new ThreadLocalRunnerSupplier(runtimeOptions, eventBus, backendSupplier, objectFactorySupplier, typeRegistryConfigurerSupplier)
                : new SingletonRunnerSupplier(runtimeOptions, eventBus, backendSupplier, objectFactorySupplier, typeRegistryConfigurerSupplier);

            final ExecutorService executor = runtimeOptions.isMultiThreaded()
                ? Executors.newFixedThreadPool(runtimeOptions.getThreads(), new CucumberThreadFactory())
                : new SameThreadExecutorService();

            final FeatureParser parser = new FeatureParser(eventBus::generateId);

            final FeatureSupplier featureSupplier = this.featureSupplier != null
                ? this.featureSupplier
                : new FeaturePathFeatureSupplier(classLoader, runtimeOptions, parser);

            final Predicate<Pickle> filter = new Filters(runtimeOptions);
            final int limit = runtimeOptions.getLimitCount();
            final PickleOrder pickleOrder = runtimeOptions.getPickleOrder();

            return new Runtime(exitStatus, eventBus, filter, limit, runnerSupplier, featureSupplier, executor, pickleOrder);
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
