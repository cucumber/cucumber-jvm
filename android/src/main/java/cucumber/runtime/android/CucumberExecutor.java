package cucumber.runtime.android;

import android.app.Instrumentation;
import android.content.Context;
import android.util.Log;
import cucumber.api.TypeRegistryConfigurer;
import cucumber.api.CucumberOptions;
import cucumber.api.StepDefinitionReporter;
import cucumber.api.event.TestRunStarted;
import cucumber.runner.EventBus;
import cucumber.runner.TimeService;
import cucumber.runtime.FeatureSupplier;
import cucumber.runtime.Filters;
import cucumber.runtime.RerunFilters;
import cucumber.runtime.model.FeatureLoader;
import cucumber.runtime.RunnerSupplier;
import cucumber.runtime.RuntimeGlueSupplier;
import cucumber.runtime.Supplier;
import io.cucumber.stepexpression.TypeRegistry;
import cucumber.api.event.TestRunFinished;
import cucumber.api.java.ObjectFactory;
import cucumber.runtime.Backend;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.CucumberException;
import cucumber.runtime.DefaultTypeRegistryConfiguration;
import cucumber.runtime.Env;
import cucumber.runtime.Reflections;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.RuntimeOptionsFactory;
import cucumber.runtime.Stats;
import cucumber.runtime.UndefinedStepsTracker;
import cucumber.runtime.formatter.AndroidInstrumentationReporter;
import cucumber.runtime.formatter.AndroidLogcatReporter;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.java.JavaBackend;
import cucumber.runtime.java.ObjectFactoryLoader;
import cucumber.runtime.model.CucumberFeature;
import dalvik.system.DexFile;
import gherkin.events.PickleEvent;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.singletonList;

/**
 * Executes the cucumber scenarios.
 */
public final class CucumberExecutor {

    /**
     * The logcat tag to log all cucumber related information to.
     */
    static final String TAG = "cucumber-android";

    /**
     * The system property name of the cucumber options.
     */
    private static final String CUCUMBER_OPTIONS_SYSTEM_PROPERTY = "cucumber.options";

    /**
     * The instrumentation to report to.
     */
    private final Instrumentation instrumentation;

    /**
     * The {@link java.lang.ClassLoader} for all test relevant classes.
     */
    private final ClassLoader classLoader;

    /**
     * The {@link cucumber.runtime.ClassFinder} to find all to be loaded classes.
     */
    private final ClassFinder classFinder;

    /**
     * The {@link cucumber.runtime.RuntimeOptions} to get the {@link CucumberFeature}s from.
     */
    private final RuntimeOptions runtimeOptions;

    /**
     * The {@link cucumber.runtime.Runtime} to run with.
     */
    private final Runtime runtime;

    private final List<PickleEvent> pickleEvents;
    private final EventBus bus;

    /**
     * Creates a new instance for the given parameters.
     *
     * @param arguments       the {@link cucumber.runtime.android.Arguments} which configure this
     *                        execution
     * @param instrumentation the {@link android.app.Instrumentation} to report to
     */
    public CucumberExecutor(final Arguments arguments, final Instrumentation instrumentation) {

        trySetCucumberOptionsToSystemProperties(arguments);

        final Context context = instrumentation.getContext();
        this.instrumentation = instrumentation;
        this.classLoader = context.getClassLoader();
        this.classFinder = createDexClassFinder(context);
        this.runtimeOptions = createRuntimeOptions(context).noSummaryPrinter();

        ResourceLoader resourceLoader = new AndroidResourceLoader(context);

        this.bus = new EventBus(TimeService.SYSTEM);
        RuntimeGlueSupplier glueSupplier = new RuntimeGlueSupplier();
        RunnerSupplier runnerSupplier = new RunnerSupplier(runtimeOptions, bus, createBackends(), glueSupplier);
        FeatureLoader featureLoader = new FeatureLoader(resourceLoader);
        FeatureSupplier featureSupplier = new FeatureSupplier(featureLoader, runtimeOptions);
        RerunFilters rerunFilters = new RerunFilters(runtimeOptions, featureLoader);
        Filters filters = new Filters(runtimeOptions, rerunFilters);
        this.runtime = new Runtime(classLoader, runtimeOptions, bus, filters, runnerSupplier, featureSupplier);
        UndefinedStepsTracker undefinedStepsTracker = new UndefinedStepsTracker();
        undefinedStepsTracker.setEventPublisher(bus);
        Stats stats = new Stats();
        stats.setEventPublisher(bus);

        AndroidInstrumentationReporter instrumentationReporter = new AndroidInstrumentationReporter(undefinedStepsTracker, instrumentation);
        runtimeOptions.addPlugin(instrumentationReporter);
        runtimeOptions.addPlugin(new AndroidLogcatReporter(stats, undefinedStepsTracker, TAG));

        // Start the run before reading the features.
        // Allows the test source read events to be broadcast properly

        List<CucumberFeature> features = featureLoader.load(runtimeOptions.getFeaturePaths(), System.out);
        runtimeOptions.setEventBus(bus);
        runtimeOptions.getPlugins(); // to create the formatter objects
        bus.send(new TestRunStarted(bus.getTime()));
        for (CucumberFeature feature : features) {
            feature.sendTestSourceRead(bus);
        }
        this.pickleEvents = FeatureCompiler.compile(features, filters);
        instrumentationReporter.setNumberOfTests(getNumberOfConcreteScenarios());
    }

    /**
     * Runs the cucumber scenarios with the specified arguments.
     */
    public void execute() {
        final StepDefinitionReporter stepDefinitionReporter = runtimeOptions.stepDefinitionReporter(classLoader);
        runtime.getRunner().reportStepDefinitions(stepDefinitionReporter);

        for (final PickleEvent pickleEvent : pickleEvents) {
            runtime.getRunner().runPickle(pickleEvent);
        }

        bus.send(new TestRunFinished(bus.getTime()));
    }

    /**
     * @return the number of actual scenarios, including outlined
     */
    public int getNumberOfConcreteScenarios() {
        return pickleEvents.size();
    }

    private void trySetCucumberOptionsToSystemProperties(final Arguments arguments) {
        final String cucumberOptions = arguments.getCucumberOptions();
        if (!cucumberOptions.isEmpty()) {
            Log.d(TAG, "Setting cucumber.options from arguments: '" + cucumberOptions + "'");
            System.setProperty(CUCUMBER_OPTIONS_SYSTEM_PROPERTY, cucumberOptions);
        }
    }

    private ClassFinder createDexClassFinder(final Context context) {
        final String apkPath = context.getPackageCodePath();
        return new DexClassFinder(newDexFile(apkPath));
    }

    private DexFile newDexFile(final String apkPath) {
        try {
            return new DexFile(apkPath);
        } catch (final IOException e) {
            throw new CucumberException("Failed to open " + apkPath);
        }
    }

    private RuntimeOptions createRuntimeOptions(final Context context) {
        for (final Class<?> clazz : classFinder.getDescendants(Object.class, context.getPackageName())) {
            if (clazz.isAnnotationPresent(CucumberOptions.class)) {
                Log.d(TAG, "Found CucumberOptions in class " + clazz.getName());
                final RuntimeOptionsFactory factory = new RuntimeOptionsFactory(clazz);
                return factory.create();
            }
        }

        throw new CucumberException("No CucumberOptions annotation");
    }

    private Supplier<Collection<? extends Backend>> createBackends() {
        return new Supplier<Collection<? extends Backend>>() {
            @Override
            public Collection<? extends Backend> get() {
                final Reflections reflections = new Reflections(classFinder);
                final ObjectFactory delegateObjectFactory = ObjectFactoryLoader.loadObjectFactory(classFinder, Env.INSTANCE.get(ObjectFactory.class.getName()));
                final AndroidObjectFactory objectFactory = new AndroidObjectFactory(delegateObjectFactory, instrumentation);
                final TypeRegistryConfigurer typeRegistryConfigurer = reflections.instantiateExactlyOneSubclass(TypeRegistryConfigurer.class, MultiLoader.packageName(runtimeOptions.getGlue()), new Class[0], new Object[0], new DefaultTypeRegistryConfiguration());
                final TypeRegistry typeRegistry = new TypeRegistry(typeRegistryConfigurer.locale());
                typeRegistryConfigurer.configureTypeRegistry(typeRegistry);
                return singletonList(new JavaBackend(objectFactory, classFinder, typeRegistry));
            }
        };

    }
}
