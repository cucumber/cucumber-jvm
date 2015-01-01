package cucumber.runtime.android;

import android.app.Instrumentation;
import android.content.Context;
import android.util.Log;
import cucumber.api.CucumberOptions;
import cucumber.api.StepDefinitionReporter;
import cucumber.runtime.Backend;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.CucumberException;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.RuntimeOptionsFactory;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.java.JavaBackend;
import cucumber.runtime.java.ObjectFactory;
import cucumber.runtime.model.CucumberFeature;
import dalvik.system.DexFile;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Executes the cucumber scnearios.
 */
public class CucumberExecutor {

    /**
     * The logcat tag to log all cucumber related information to.
     */
    public static final String TAG = "cucumber-android";

    /**
     * The system property name of the cucumber options.
     */
    public static final String CUCUMBER_OPTIONS_SYSTEM_PROPERTY = "cucumber.options";

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
     * The actual {@link CucumberFeature}s to run.
     */
    private final List<CucumberFeature> cucumberFeatures;

    /**
     * Creates a new instance for the given parameters.
     *
     * @param arguments       the {@link cucumber.runtime.android.Arguments} which configure this execution
     * @param instrumentation the {@link android.app.Instrumentation} to report to
     */
    public CucumberExecutor(final Arguments arguments, final Instrumentation instrumentation) {

        trySetCucumberOptionsToSystemProperties(arguments);

        final Context context = instrumentation.getContext();
        this.instrumentation = instrumentation;
        this.classLoader = context.getClassLoader();
        this.classFinder = createDexClassFinder(context);
        this.runtimeOptions = createRuntimeOptions(context);

        ResourceLoader resourceLoader = new AndroidResourceLoader(context);
        this.runtime = new Runtime(resourceLoader, classLoader, createBackends(), runtimeOptions);
        this.cucumberFeatures = runtimeOptions.cucumberFeatures(resourceLoader);
    }

    /**
     * Runs the cucumber scenarios with the specified arguments.
     */
    public void execute() {

        runtimeOptions.addPlugin(new AndroidInstrumentationReporter(runtime, instrumentation, getNumberOfConcreteScenarios()));
        runtimeOptions.addPlugin(new AndroidLogcatReporter(runtime, TAG));

        // TODO: This is duplicated in info.cucumber.Runtime.

        final Reporter reporter = runtimeOptions.reporter(classLoader);
        final Formatter formatter = runtimeOptions.formatter(classLoader);

        final StepDefinitionReporter stepDefinitionReporter = runtimeOptions.stepDefinitionReporter(classLoader);
        runtime.getGlue().reportStepDefinitions(stepDefinitionReporter);

        for (final CucumberFeature cucumberFeature : cucumberFeatures) {
            cucumberFeature.run(formatter, reporter, runtime);
        }

        formatter.done();
        formatter.close();
    }

    /**
     * @return the number of actual scenarios, including outlined
     */
    public int getNumberOfConcreteScenarios() {
        return ScenarioCounter.countScenarios(cucumberFeatures);
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
        final ObjectFactory delegateObjectFactory = JavaBackend.loadObjectFactory(classFinder);
        final AndroidObjectFactory objectFactory = new AndroidObjectFactory(delegateObjectFactory, instrumentation);
        final List<Backend> backends = new ArrayList<Backend>();
        backends.add(new JavaBackend(objectFactory, classFinder));
        return backends;
    }
}
