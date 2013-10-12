package cucumber.api.android;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.os.Bundle;
import android.os.Debug;
import android.os.Looper;
import android.util.Log;
import cucumber.api.CucumberOptions;
import cucumber.runtime.Backend;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.CucumberException;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.RuntimeOptionsFactory;
import cucumber.runtime.android.AndroidInstrumentationReporter;
import cucumber.runtime.android.AndroidLogcatReporter;
import cucumber.runtime.android.AndroidObjectFactory;
import cucumber.runtime.android.AndroidResourceLoader;
import cucumber.runtime.android.InstrumentationArguments;
import cucumber.runtime.android.DexClassFinder;
import cucumber.runtime.android.TestCaseCounter;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.java.JavaBackend;
import cucumber.runtime.java.ObjectFactory;
import cucumber.runtime.model.CucumberFeature;
import dalvik.system.DexFile;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class CucumberInstrumentation extends Instrumentation {
    public static final String REPORT_VALUE_ID = "CucumberInstrumentation";
    public static final String REPORT_KEY_NUM_TOTAL = "numtests";
    private static final String REPORT_KEY_COVERAGE_PATH = "coverageFilePath";
    private static final String DEFAULT_COVERAGE_FILE_NAME = "coverage.ec";
    public static final String TAG = "cucumber-android";

    private final Bundle results = new Bundle();
    private boolean justCount;
    private int testCount;
    private boolean coverage;
    private String coverageFilePath;

    private RuntimeOptions runtimeOptions;
    private ResourceLoader resourceLoader;
    private ClassLoader classLoader;
    private Runtime runtime;
    private boolean debug;
    private List<CucumberFeature> cucumberFeatures;

    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);

        if (arguments != null) {
            debug = getBooleanArgument(arguments, "debug");
            justCount = getBooleanArgument(arguments, "count");
            coverage = getBooleanArgument(arguments, "coverage");
            coverageFilePath = arguments.getString("coverageFile");
        }

        InstrumentationArguments instrumentationArguments = new InstrumentationArguments(arguments);

        Context context = getContext();
        classLoader = context.getClassLoader();

        String apkPath = context.getPackageCodePath();
        ClassFinder classFinder = new DexClassFinder(newDexFile(apkPath));

        Class<?> optionsAnnotatedClass = null;
        for (Class<?> clazz : classFinder.getDescendants(Object.class, context.getPackageName())) {
            if (clazz.isAnnotationPresent(CucumberOptions.class)) {
                Log.d(TAG, "Found CucumberOptions in class " + clazz.getName());
                Log.d(TAG, clazz.getAnnotations()[0].toString());
                optionsAnnotatedClass = clazz;
                break; // We assume there is only one CucumberOptions annotated class.
            }
        }
        if (optionsAnnotatedClass == null) {
            throw new CucumberException("No CucumberOptions annotation");
        }

        String cucumberOptions = instrumentationArguments.getCucumberOptionsString();
        if (!cucumberOptions.isEmpty()) {
            Log.d(TAG, "Setting cucumber.options from arguments: '" + cucumberOptions + "'");
            System.setProperty("cucumber.options", cucumberOptions);
        }

        @SuppressWarnings("unchecked")
        RuntimeOptionsFactory factory = new RuntimeOptionsFactory(optionsAnnotatedClass, new Class[]{CucumberOptions.class});
        runtimeOptions = factory.create();
        resourceLoader = new AndroidResourceLoader(context);

        List<Backend> backends = new ArrayList<Backend>();
        ObjectFactory delegateObjectFactory = JavaBackend.loadObjectFactory(classFinder);
        AndroidObjectFactory objectFactory = new AndroidObjectFactory(delegateObjectFactory, this);
        backends.add(new JavaBackend(objectFactory, classFinder));
        runtime = new Runtime(resourceLoader, classLoader, backends, runtimeOptions);
        cucumberFeatures = runtimeOptions.cucumberFeatures(resourceLoader);
        testCount = TestCaseCounter.countTestCasesOf(cucumberFeatures);

        start();
    }

    private DexFile newDexFile(String apkPath) {
        try {
            return new DexFile(apkPath);
        } catch (IOException e) {
            throw new CucumberException("Failed to open " + apkPath);
        }
    }

    @Override
    public void onStart() {
        Looper.prepare();

        if (justCount) {
            results.putString(Instrumentation.REPORT_KEY_IDENTIFIER, REPORT_VALUE_ID);
            results.putInt(REPORT_KEY_NUM_TOTAL, testCount);
            finish(Activity.RESULT_OK, results);
        } else {
            if (debug) {
                Debug.waitForDebugger();
            }

            runtimeOptions.getFormatters().add(new AndroidInstrumentationReporter(runtime, this, testCount));
            runtimeOptions.getFormatters().add(new AndroidLogcatReporter(TAG));

            final Reporter reporter = runtimeOptions.reporter(classLoader);
            final Formatter formatter = runtimeOptions.formatter(classLoader);

            for (final CucumberFeature cucumberFeature : cucumberFeatures) {
                cucumberFeature.run(formatter, reporter, runtime);
            }

            formatter.done();
            formatter.close();

            printSummary();

            if (coverage) {
                generateCoverageReport();
            }

            finish(Activity.RESULT_OK, results);
        }
    }

    private void printSummary() {
        // TODO move this stuff into the AndroidLogcatReporter
        for (Throwable t : runtime.getErrors()) {
            Log.e(TAG, t.toString());
        }
        for (String s : runtime.getSnippets()) {
            Log.w(TAG, s);
        }
    }

    private boolean getBooleanArgument(Bundle arguments, String tag) {
        String tagString = arguments.getString(tag);
        return tagString != null && Boolean.parseBoolean(tagString);
    }

    private void generateCoverageReport() {
        // use reflection to call emma dump coverage method, to avoid
        // always statically compiling against emma jar
        String coverageFilePath = getCoverageFilePath();
        java.io.File coverageFile = new java.io.File(coverageFilePath);
        try {
            Class<?> emmaRTClass = Class.forName("com.vladium.emma.rt.RT");
            Method dumpCoverageMethod = emmaRTClass.getMethod("dumpCoverageData",
                    coverageFile.getClass(), boolean.class, boolean.class);

            dumpCoverageMethod.invoke(null, coverageFile, false, false);
            // output path to generated coverage file so it can be parsed by a test harness if
            // needed
            results.putString(REPORT_KEY_COVERAGE_PATH, coverageFilePath);
            // also output a more user friendly msg
            final String currentStream = results.getString(
                    Instrumentation.REPORT_KEY_STREAMRESULT);
            results.putString(Instrumentation.REPORT_KEY_STREAMRESULT,
                String.format("%s\nGenerated code coverage data to %s", currentStream,
                coverageFilePath));
        } catch (ClassNotFoundException e) {
            reportEmmaError("Is emma jar on classpath?", e);
        } catch (SecurityException e) {
            reportEmmaError(e);
        } catch (NoSuchMethodException e) {
            reportEmmaError(e);
        } catch (IllegalArgumentException e) {
            reportEmmaError(e);
        } catch (IllegalAccessException e) {
            reportEmmaError(e);
        } catch (InvocationTargetException e) {
            reportEmmaError(e);
        }
    }

    private String getCoverageFilePath() {
        if (coverageFilePath == null) {
            return getTargetContext().getFilesDir().getAbsolutePath() + File.separator +
                   DEFAULT_COVERAGE_FILE_NAME;
        } else {
            return coverageFilePath;
        }
    }

    private void reportEmmaError(Exception e) {
        reportEmmaError("", e);
    }

    private void reportEmmaError(String hint, Exception e) {
        String msg = "Failed to generate emma coverage. " + hint;
        Log.e(TAG, msg, e);
        results.putString(Instrumentation.REPORT_KEY_STREAMRESULT, "\nError: " + msg);
    }
}
