package cucumber.api.android;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.os.Bundle;
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
import cucumber.runtime.android.DexClassFinder;
import cucumber.runtime.android.TestCaseCounter;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.java.JavaBackend;
import cucumber.runtime.java.ObjectFactory;
import cucumber.runtime.model.CucumberFeature;
import dalvik.system.DexFile;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CucumberInstrumentation extends Instrumentation {
    public static final String TAG = "cucumber-android";
    private RuntimeOptions runtimeOptions;
    private ResourceLoader resourceLoader;
    private ClassLoader classLoader;
    private Runtime runtime;

    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);

        if (arguments == null) {
            throw new CucumberException("No arguments");
        }
        Context context = getContext();
        classLoader = context.getClassLoader();

        String apkPath = context.getPackageCodePath();
        ClassFinder classFinder = new DexClassFinder(newDexFile(apkPath));

        Class<?> optionsAnnotatedClass = null;
        for (Class<?> clazz : classFinder.getDescendants(Object.class, context.getPackageName())) {
            if (clazz.isAnnotationPresent(CucumberOptions.class)) {
                Log.d(TAG, "Found CucumberOptions in class " + clazz.getName());
                optionsAnnotatedClass = clazz;
                break; // We assume there is only one CucumberOptions annotated class.
            }
        }
        if (optionsAnnotatedClass == null) {
            throw new CucumberException("No CucumberOptions annotation");
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

        final List<CucumberFeature> cucumberFeatures = runtimeOptions.cucumberFeatures(resourceLoader);
        final int numberOfTests = TestCaseCounter.countTestCasesOf(cucumberFeatures);

        runtimeOptions.getFormatters().clear();
        runtimeOptions.getFormatters().add(new AndroidInstrumentationReporter(runtime, this, numberOfTests));
        runtimeOptions.getFormatters().add(new AndroidLogcatReporter(TAG));

        final Reporter reporter = runtimeOptions.reporter(classLoader);
        final Formatter formatter = runtimeOptions.formatter(classLoader);

        for (final CucumberFeature cucumberFeature : cucumberFeatures) {
            cucumberFeature.run(formatter, reporter, runtime);
        }

        formatter.done();
        formatter.close();

        printSummary();

        finish(Activity.RESULT_OK, new Bundle());
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
}
