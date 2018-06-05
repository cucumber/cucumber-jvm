package cucumber.runtime.android;

import android.app.Instrumentation;
import android.content.Context;
import android.util.Log;
import cucumber.api.TypeRegistryConfigurer;
import cucumber.api.CucumberOptions;
import cucumber.api.StepDefinitionReporter;
import cucumber.api.event.TestRunStarted;
import cucumber.runner.EventBus;
import cucumber.runner.Runner;
import cucumber.runner.TimeService;
import cucumber.runtime.BackendSupplier;
import cucumber.runtime.FeaturePathFeatureSupplier;
import cucumber.runtime.filter.Filters;
import cucumber.runtime.formatter.Plugins;
import cucumber.runtime.filter.RerunFilters;
import cucumber.runtime.formatter.PluginFactory;
import cucumber.runtime.model.FeatureLoader;
import cucumber.runtime.ThreadLocalRunnerSupplier;
import cucumber.runtime.RuntimeGlueSupplier;
import io.cucumber.stepexpression.TypeRegistry;
import cucumber.api.event.TestRunFinished;
import cucumber.api.java.ObjectFactory;
import cucumber.runtime.Backend;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.CucumberException;
import cucumber.runtime.DefaultTypeRegistryConfiguration;
import cucumber.runtime.Env;
import cucumber.runtime.Reflections;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.RuntimeOptionsFactory;
import cucumber.runtime.formatter.Stats;
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
     * The {@link cucumber.runtime.ClassFinder} to find all to be loaded classes.
     */
    private final ClassFinder classFinder;

    /**
     * The {@link cucumber.runtime.RuntimeOptions} to get the {@link CucumberFeature}s from.
     */
    private final RuntimeOptions runtimeOptions;

    private final List<PickleEvent> pickleEvents;
    private final EventBus bus;
    private final Plugins plugins;
    private final Runner runner;

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
        ClassLoader classLoader = context.getClassLoader();
        this.classFinder = createDexClassFinder(context);
        this.runtimeOptions = createRuntimeOptions(context).noSummaryPrinter();

        ResourceLoader resourceLoader = new AndroidResourceLoader(context);

        this.bus = new EventBus(TimeService.SYSTEM);
        this.plugins = new Plugins(classLoader, new PluginFactory(), bus, runtimeOptions);
        RuntimeGlueSupplier glueSupplier = new RuntimeGlueSupplier();
        this.runner = new ThreadLocalRunnerSupplier(runtimeOptions, bus, createBackends(), glueSupplier).get();
        FeatureLoader featureLoader = new FeatureLoader(resourceLoader);
        FeaturePathFeatureSupplier featureSupplier = new FeaturePathFeatureSupplier(featureLoader, runtimeOptions);
        RerunFilters rerunFilters = new RerunFilters(runtimeOptions, featureLoader);
        Filters filters = new Filters(runtimeOptions, rerunFilters);
        UndefinedStepsTracker undefinedStepsTracker = new UndefinedStepsTracker();
        undefinedStepsTracker.setEventPublisher(bus);
        Stats stats = new Stats();
        stats.setEventPublisher(bus);

        AndroidInstrumentationReporter instrumentationReporter = new AndroidInstrumentationReporter(undefinedStepsTracker, instrumentation);
        plugins.addPlugin(instrumentationReporter);
        plugins.addPlugin(new AndroidLogcatReporter(stats, undefinedStepsTracker, TAG));

        // Start the run before reading the features.
        // Allows the test source read events to be broadcast properly
        List<CucumberFeature> features = featureSupplier.get();
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
        final StepDefinitionReporter stepDefinitionReporter = plugins.stepDefinitionReporter();
        runner.reportStepDefinitions(stepDefinitionReporter);
        for (final PickleEvent pickleEvent : pickleEvents) {
            runner.runPickle(pickleEvent);
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

    private BackendSupplier createBackends() {
        return new BackendSupplier() {
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
