package cucumber.api.android;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import cucumber.runtime.android.AndroidBackend;
import cucumber.runtime.android.AndroidClasspathMethodScanner;
import cucumber.runtime.android.AndroidFormatter;
import cucumber.runtime.android.AndroidResourceLoader;
import cucumber.runtime.Backend;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.model.*;
import ext.android.test.ClassPathPackageInfoSource;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class CucumberInstrumentation extends Instrumentation {
    public static final String ARGUMENT_TEST_CLASS = "class";
    public static final String ARGUMENT_TEST_PACKAGE = "package";
    public static final String REPORT_VALUE_ID = "InstrumentationTestRunner";
    public static final String REPORT_KEY_NUM_TOTAL = "numtests";
    public static final String REPORT_KEY_NUM_CURRENT = "current";
    public static final String REPORT_KEY_NAME_CLASS = "class";
    public static final String REPORT_KEY_NAME_TEST = "test";
    public static final int REPORT_VALUE_RESULT_START = 1;
    public static final int REPORT_VALUE_RESULT_ERROR = -1;
    public static final int REPORT_VALUE_RESULT_FAILURE = -2;
    public static final String REPORT_KEY_STACK = "stack";
    public static final String TAG = "cucumber-android";
    private RuntimeOptions mRuntimeOptions;
    private ResourceLoader mResourceLoader;
    private ClassLoader mClassLoader;
    private Runtime mRuntime;
    private String mPackageOfTests;
    private String mFeatures;

    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);

        Context context = getContext();
        mClassLoader = context.getClassLoader();

        // For glue and features either use the provided arguments or try to find a RunWithCucumber annotated class.
        // If nothing works, default values will be used instead.
        if (arguments != null &&
                (arguments.containsKey(ARGUMENT_TEST_CLASS) || arguments.containsKey(ARGUMENT_TEST_PACKAGE))) {

            String testClass = arguments.getString(ARGUMENT_TEST_CLASS);
            testClass = testClass != null ? testClass : "null";
            mPackageOfTests = arguments.getString(ARGUMENT_TEST_PACKAGE);

            try {
                Class<?> clazz = mClassLoader.loadClass(testClass);
                boolean annotationWasPresent = readRunWithCucumberAnnotation(clazz);

                // If the class is not RunWithCucumber annotated, maybe it's Cucumber annotated?
                if (!annotationWasPresent) {
                    SEARCH_ANNOTATION:
                    for (Method m : clazz.getMethods()) {
                        for (Annotation a : m.getAnnotations()) {
                            if (a.annotationType().getName().startsWith("cucumber") && mPackageOfTests == null) {
                                mPackageOfTests = testClass.substring(0, testClass.lastIndexOf("."));
                                break SEARCH_ANNOTATION;
                            }
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                Log.w(TAG, e.toString());
            }
        } else {
            ClassPathPackageInfoSource source = AndroidClasspathMethodScanner.classPathPackageInfoSource(context);
            for (Class<?> clazz : source.getPackageInfo(context.getPackageName()).getTopLevelClassesRecursive()) {
                if (readRunWithCucumberAnnotation(clazz)) break;
            }
        }

        Properties properties = new Properties();
        mPackageOfTests = mPackageOfTests != null ? mPackageOfTests : defaultGlue();
        mFeatures = mFeatures != null ? mFeatures : defaultFeatures();

        properties.setProperty("cucumber.options", String.format("-g %s %s", mPackageOfTests, mFeatures));
        mRuntimeOptions = new RuntimeOptions(properties);

        mResourceLoader = new AndroidResourceLoader(context);
        List<Backend> backends = new ArrayList<Backend>();
        backends.add(new AndroidBackend(this));
        mRuntime = new Runtime(mResourceLoader, mClassLoader, backends, mRuntimeOptions);

        start();
    }

    /**
     * @return true if the class is RunWithCucumber annotated, false otherwise
     */
    private boolean readRunWithCucumberAnnotation(Class<?> clazz) {
        RunWithCucumber annotation = clazz.getAnnotation(RunWithCucumber.class);
        if (annotation != null) {
            mPackageOfTests = annotation.glue().isEmpty() ? defaultGlue() : annotation.glue();
            mFeatures = annotation.features().isEmpty() ? defaultFeatures() : annotation.features();
            return true;
        }
        return false;
    }

    private String defaultFeatures() {
        return "features";
    }

    private String defaultGlue() {
        return getContext().getPackageName();
    }

    @Override
    public void onStart() {
        Looper.prepare();

        List<CucumberFeature> cucumberFeatures = mRuntimeOptions.cucumberFeatures(mResourceLoader);
        int numScenarios = 0;

        for (CucumberFeature feature : cucumberFeatures) {
            for (CucumberTagStatement statement : feature.getFeatureElements()) {
                if (statement instanceof CucumberScenario) {
                    numScenarios++;
                } else if (statement instanceof CucumberScenarioOutline) {
                    for (CucumberExamples examples : ((CucumberScenarioOutline) statement).getCucumberExamplesList()) {
                        for (ExamplesTableRow row : examples.getExamples().getRows()) {
                            numScenarios++;
                        }
                    }
                    numScenarios--; // subtract table header
                }
            }
        }

        AndroidReporter reporter = new AndroidReporter(numScenarios);
        mRuntimeOptions.formatters.clear();
        mRuntimeOptions.formatters.add(reporter);

        for (CucumberFeature cucumberFeature : cucumberFeatures) {
            Formatter formatter = mRuntimeOptions.formatter(mClassLoader);
            cucumberFeature.run(formatter, reporter, mRuntime);
        }
        Formatter formatter = mRuntimeOptions.formatter(mClassLoader);

        formatter.done();
        printSummary();
        formatter.close();

        finish(Activity.RESULT_OK, new Bundle());
    }

    private void printSummary() {
        for (Throwable t : mRuntime.getErrors()) {
            Log.e(TAG, t.toString());
        }
        for (String s : mRuntime.getSnippets()) {
            Log.w(TAG, s);
        }
    }

    /**
     * This class reports the current test-state back to the framework.
     * It also wraps the AndroidFormatter to intercept important callbacks.
     */
    private class AndroidReporter implements Formatter, Reporter {
        private final AndroidFormatter mFormatter;
        private final Bundle mResultTemplate;
        private Bundle mTestResult;
        private int mScenarioNum;
        private int mTestResultCode;
        private Feature mFeature;
        private Step mStep;

        public AndroidReporter(int numTests) {
            mFormatter = new AndroidFormatter(TAG);
            mResultTemplate = new Bundle();
            mResultTemplate.putString(Instrumentation.REPORT_KEY_IDENTIFIER, REPORT_VALUE_ID);
            mResultTemplate.putInt(REPORT_KEY_NUM_TOTAL, numTests);
        }

        @Override
        public void uri(String uri) {
            mFormatter.uri(uri);
        }

        @Override
        public void feature(Feature feature) {
            mFeature = feature;
            mFormatter.feature(feature);
        }

        @Override
        public void background(Background background) {
            mFormatter.background(background);
        }

        @Override
        public void scenario(Scenario scenario) {
            reportLastResult();
            mFormatter.scenario(scenario);
            beginScenario(scenario);
        }

        @Override
        public void scenarioOutline(ScenarioOutline scenarioOutline) {
            reportLastResult();
            mFormatter.scenarioOutline(scenarioOutline);
            beginScenario(scenarioOutline);
        }

        @Override
        public void examples(Examples examples) {
            mFormatter.examples(examples);
        }

        @Override
        public void step(Step step) {
            mStep = step;
            mFormatter.step(step);
        }

        @Override
        public void syntaxError(String state, String event, List<String> legalEvents, String uri, Integer line) {
            mFormatter.syntaxError(state, event, legalEvents, uri, line);
        }

        @Override
        public void eof() {
            reportLastResult();
            mFormatter.eof();
        }

        @Override
        public void done() {
            mFormatter.done();
        }

        @Override
        public void close() {
            mFormatter.close();
        }

        @Override
        public void embedding(String mimeType, byte[] data) {
        }

        @Override
        public void write(String text) {
        }

        @Override
        public void before(Match match, Result result) {
        }

        @Override
        public void after(Match match, Result result) {
        }

        @Override
        public void match(Match match) {
        }

        private void beginScenario(TagStatement scenario) {
            String testClass = String.format("%s %s", mFeature.getKeyword(), mFeature.getName());
            String testName = String.format("%s %s", scenario.getKeyword(), scenario.getName());
            mTestResult = new Bundle(mResultTemplate);
            mTestResult.putString(REPORT_KEY_NAME_CLASS, testClass);
            mTestResult.putString(REPORT_KEY_NAME_TEST, testName);
            mTestResult.putInt(REPORT_KEY_NUM_CURRENT, ++mScenarioNum);

            mTestResult.putString(Instrumentation.REPORT_KEY_STREAMRESULT, String.format("\n%s:", testClass));

            sendStatus(REPORT_VALUE_RESULT_START, mTestResult);
            mTestResultCode = 0;
        }

        @Override
        public void result(Result result) {
            if (result.getError() != null) {
                // If the result contains an error, report a failure.
                mTestResult.putString(REPORT_KEY_STACK, result.getErrorMessage());
                mTestResultCode = REPORT_VALUE_RESULT_FAILURE;
                mTestResult.putString(Instrumentation.REPORT_KEY_STREAMRESULT, result.getErrorMessage());
            } else if (result.getStatus().equals("undefined")) {
                // There was a missing step definition, report an error.
                List<String> snippets = mRuntime.getSnippets();
                String report = String.format("Missing step-definition\n\n%s\nfor step '%s'",
                        snippets.get(snippets.size() - 1),
                        mStep.getName());
                mTestResult.putString(REPORT_KEY_STACK, report);
                mTestResultCode = REPORT_VALUE_RESULT_ERROR;
                mTestResult.putString(Instrumentation.REPORT_KEY_STREAMRESULT,
                        String.format("Missing step-definition: %s", mStep.getName()));
            }
        }

        private void reportLastResult() {
            if (mScenarioNum != 0) {
                if (mTestResultCode == 0) {
                    mTestResult.putString(Instrumentation.REPORT_KEY_STREAMRESULT, ".");
                }
                sendStatus(mTestResultCode, mTestResult);
            }
        }
    }
}
