package io.cucumber.testng;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.exception.CompositeCucumberException;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.feature.FeatureParser;
import io.cucumber.core.filter.Filters;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import io.cucumber.core.options.Constants;
import io.cucumber.core.options.CucumberOptionsAnnotationParser;
import io.cucumber.core.options.CucumberProperties;
import io.cucumber.core.options.CucumberPropertiesParser;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.plugin.PluginFactory;
import io.cucumber.core.plugin.Plugins;
import io.cucumber.core.resource.ClassLoaders;
import io.cucumber.core.runner.Runner;
import io.cucumber.core.runtime.BackendServiceLoader;
import io.cucumber.core.runtime.ExitStatus;
import io.cucumber.core.runtime.FeaturePathFeatureSupplier;
import io.cucumber.core.runtime.ObjectFactoryServiceLoader;
import io.cucumber.core.runtime.ObjectFactorySupplier;
import io.cucumber.core.runtime.ScanningTypeRegistryConfigurerSupplier;
import io.cucumber.core.runtime.ThreadLocalObjectFactorySupplier;
import io.cucumber.core.runtime.ThreadLocalRunnerSupplier;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.core.runtime.TypeRegistryConfigurerSupplier;
import io.cucumber.messages.Messages;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import io.cucumber.plugin.event.TestSourceRead;
import org.apiguardian.api.API;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static io.cucumber.messages.TimeConversion.javaInstantToTimestamp;
import static java.util.Collections.synchronizedList;
import static java.util.stream.Collectors.toList;

/**
 * Glue code for running Cucumber via TestNG.
 * <p>
 * Options can be provided in by (order of precedence):
 * <ol>
 * <li>Properties from {@link System#getProperties()}</li>
 * <li>Properties from in {@link System#getenv()}</li>
 * <li>Annotating the runner class with {@link CucumberOptions}</li>
 * <li>Properties from {@value Constants#CUCUMBER_PROPERTIES_FILE_NAME}</li>
 * </ol>
 * For available properties see {@link Constants}.
 */
@API(status = API.Status.STABLE)
public final class TestNGCucumberRunner {

    private static final Logger log = LoggerFactory.getLogger(TestNGCucumberRunner.class);

    private final EventBus bus;
    private final Predicate<Pickle> filters;
    private final ThreadLocalRunnerSupplier runnerSupplier;
    private final RuntimeOptions runtimeOptions;
    private final List<Feature> features;
    private final ExitStatus exitStatus;
    private final List<Throwable> thrown = synchronizedList(new ArrayList<>());

    /**
     * Bootstrap the cucumber runtime
     *
     * @param clazz Which has the {@link CucumberOptions}
     *              and {@link org.testng.annotations.Test} annotations
     */
    public TestNGCucumberRunner(Class<?> clazz) {
        // Parse the options early to provide fast feedback about invalid options
        RuntimeOptions propertiesFileOptions = new CucumberPropertiesParser()
            .parse(CucumberProperties.fromPropertiesFile())
            .build();

        RuntimeOptions annotationOptions = new CucumberOptionsAnnotationParser()
            .withOptionsProvider(new TestNGCucumberOptionsProvider())
            .parse(clazz)
            .build(propertiesFileOptions);

        RuntimeOptions environmentOptions = new CucumberPropertiesParser()
            .parse(CucumberProperties.fromEnvironment())
            .build(annotationOptions);

        this.runtimeOptions = new CucumberPropertiesParser()
            .parse(CucumberProperties.fromSystemProperties())
            .addDefaultSummaryPrinterIfAbsent()
            .build(environmentOptions);

        this.bus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);

        if (!runtimeOptions.isStrict()) {
            log.warn(() -> "By default Cucumber is running in --non-strict mode.\n" +
                "This default will change to --strict and --non-strict will be removed.\n" +
                "You can use --strict or @CucumberOptions(strict = true) to suppress this warning"
            );
        }

        Supplier<ClassLoader> classLoader = ClassLoaders::getDefaultClassLoader;
        FeatureParser parser = new FeatureParser(bus::generateId);
        FeaturePathFeatureSupplier featureSupplier = new FeaturePathFeatureSupplier(classLoader, runtimeOptions, parser);

        Plugins plugins = new Plugins(new PluginFactory(), runtimeOptions);
        this.exitStatus = new ExitStatus(runtimeOptions);
        plugins.addPlugin(exitStatus);
        ObjectFactoryServiceLoader objectFactoryServiceLoader = new ObjectFactoryServiceLoader(runtimeOptions);
        ObjectFactorySupplier objectFactorySupplier = new ThreadLocalObjectFactorySupplier(objectFactoryServiceLoader);
        BackendServiceLoader backendSupplier = new BackendServiceLoader(clazz::getClassLoader, objectFactorySupplier);
        this.filters = new Filters(runtimeOptions);
        TypeRegistryConfigurerSupplier typeRegistryConfigurerSupplier = new ScanningTypeRegistryConfigurerSupplier(classLoader, runtimeOptions);
        this.runnerSupplier = new ThreadLocalRunnerSupplier(runtimeOptions, bus, backendSupplier, objectFactorySupplier, typeRegistryConfigurerSupplier);

        // Start test execution now.
        plugins.setSerialEventBusOnEventListenerPlugins(bus);
        features = featureSupplier.get();
        emitTestRunStarted();
        features.forEach(this::emitTestSource);
    }

    public void runScenario(io.cucumber.testng.Pickle pickle) throws Throwable {
        //Possibly invoked in a multi-threaded context
        Runner runner = getRunner();
        try (TestCaseResultObserver observer = TestCaseResultObserver.observe(runner.getBus(), runtimeOptions.isStrict())) {
            Pickle cucumberPickle = pickle.getPickle();
            runner.runPickle(cucumberPickle);
            observer.assertTestCasePassed();
        }
    }

    private Runner getRunner() {
        try {
            return runnerSupplier.get();
        } catch (Throwable e) {
            thrown.add(e);
            throw e;
        }
    }


    /**
     * Finishes test execution by Cucumber.
     */
    public void finish() {
        if(thrown.isEmpty()){
            emitTestRunFinished(null);
        } else if (thrown.size() == 1) {
            CucumberException cucumberException = new CucumberException(thrown.get(0));
            emitTestRunFinished(cucumberException);
        } else {
            CompositeCucumberException compositeCucumberException = new CompositeCucumberException(thrown);
            emitTestRunFinished(compositeCucumberException);
        }
    }

    /**
     * @return returns the cucumber scenarios as a two dimensional array of
     * {@link PickleWrapper} scenarios combined with their
     * {@link FeatureWrapper} feature.
     */
    public Object[][] provideScenarios() {
        //Possibly invoked in a multi-threaded context
        try {
            return features.stream()
                .flatMap(feature -> feature.getPickles().stream()
                    .filter(filters)
                    .map(cucumberPickle -> new Object[]{
                        new PickleWrapperImpl(new io.cucumber.testng.Pickle(cucumberPickle)),
                        new FeatureWrapperImpl(feature)}))
                .collect(toList())
                .toArray(new Object[0][0]);
        } catch (CucumberException e) {
            return new Object[][]{new Object[]{new CucumberExceptionWrapper(e), null}};
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
}
