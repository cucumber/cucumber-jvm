package cucumber.runtime.android;

import android.app.Instrumentation;
import android.content.Context;
import android.util.Log;
import cucumber.api.TypeRegistryConfigurer;
import cucumber.api.CucumberOptions;
import cucumber.api.StepDefinitionReporter;
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

    /**
     * The actual {@link PickleEvent}s to run stored in {@link PickleStruct}s.
     */
    private final List<PickleEvent> pickleEvents;

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
        this.runtime = new Runtime(resourceLoader, classLoader, createBackends(), runtimeOptions);
        UndefinedStepsTracker undefinedStepsTracker = new UndefinedStepsTracker();
        undefinedStepsTracker.setEventPublisher(runtime.getEventBus());
        Stats stats = new Stats();
        stats.setEventPublisher(runtime.getEventBus());

        AndroidInstrumentationReporter instrumentationReporter = new AndroidInstrumentationReporter(undefinedStepsTracker, instrumentation);
        runtimeOptions.addPlugin(instrumentationReporter);
        runtimeOptions.addPlugin(new AndroidLogcatReporter(stats, undefinedStepsTracker, TAG));

        List<CucumberFeature> cucumberFeatures = runtimeOptions.cucumberFeatures(resourceLoader, runtime.getEventBus());
        this.pickleEvents = FeatureCompiler.compile(cucumberFeatures, this.runtime);
        instrumentationReporter.setNumberOfTests(getNumberOfConcreteScenarios());
    }

    /**
     * Runs the cucumber scenarios with the specified arguments.
     */
    public void execute() {

        // TODO: This is duplicated in info.cucumber.Runtime.

        final StepDefinitionReporter stepDefinitionReporter = runtimeOptions.stepDefinitionReporter(classLoader);
        runtime.reportStepDefinitions(stepDefinitionReporter);

        for (final PickleEvent pickleEvent : pickleEvents) {
            runtime.getRunner().runPickle(pickleEvent);
        }

        runtime.getEventBus().send(new TestRunFinished(runtime.getEventBus().getTime()));
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

    private Collection<? extends Backend> createBackends() {
        final Reflections reflections = new Reflections(classFinder);
        final ObjectFactory delegateObjectFactory = ObjectFactoryLoader.loadObjectFactory(classFinder, Env.INSTANCE.get(ObjectFactory.class.getName()));
        final AndroidObjectFactory objectFactory = new AndroidObjectFactory(delegateObjectFactory, instrumentation);
        final TypeRegistryConfigurer typeRegistryConfigurer = reflections.instantiateExactlyOneSubclass(TypeRegistryConfigurer.class, MultiLoader.packageName(runtimeOptions.getGlue()), new Class[0], new Object[0], new DefaultTypeRegistryConfiguration());
        final TypeRegistry typeRegistry = new TypeRegistry(typeRegistryConfigurer.locale());
        typeRegistryConfigurer.configureTypeRegistry(typeRegistry);
        return singletonList(new JavaBackend(objectFactory, classFinder, typeRegistry));
    }
}
