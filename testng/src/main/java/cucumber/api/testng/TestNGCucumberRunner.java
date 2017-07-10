package cucumber.api.testng;

import cucumber.api.event.TestRunFinished;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.CucumberException;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.RuntimeOptionsFactory;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.runtime.model.CucumberFeature;
import gherkin.events.PickleEvent;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Glue code for running Cucumber via TestNG.
 */
public class TestNGCucumberRunner {
    private Runtime runtime;
    private TestNGReporter reporter;
    private RuntimeOptions runtimeOptions;
    private ResourceLoader resourceLoader;
    private FeatureResultListener resultListener;
    private TestCaseResultListener testCaseResultListener;

    /**
     * Bootstrap the cucumber runtime
     *
     * @param clazz Which has the cucumber.api.CucumberOptions and org.testng.annotations.Test annotations
     */
    public TestNGCucumberRunner(Class clazz) {
        ClassLoader classLoader = clazz.getClassLoader();
        resourceLoader = new MultiLoader(classLoader);

        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(clazz);
        runtimeOptions = runtimeOptionsFactory.create();

        reporter = new TestNGReporter(new PrintStream(System.out) {
                @Override
                public void close() {
                    // We have no intention to close System.out
                }
            });
        ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        resultListener = new FeatureResultListener(runtimeOptions.isStrict());
        runtime = new Runtime(resourceLoader, classFinder, classLoader, runtimeOptions);
        reporter.setEventPublisher(runtime.getEventBus());
        resultListener.setEventPublisher(runtime.getEventBus());
        testCaseResultListener = new TestCaseResultListener(runtimeOptions.isStrict());
        testCaseResultListener.setEventPublisher(runtime.getEventBus());
    }

    /**
     * Run the Cucumber features
     */
    public void runCukes() {
        System.err.println("WARNING: The TestNGCucumberRunner.runCukes() is deprecated. Please create a runner class by subclassing AbstractTestNGCucumberTest.");
        for (CucumberFeature cucumberFeature : getFeatures()) {
            reporter.uri(cucumberFeature.getPath());
            runtime.runFeature(cucumberFeature);
        }
        finish();
        if (!resultListener.isPassed()) {
            throw new CucumberException(resultListener.getFirstError());
        }
    }

    public void runCucumber(CucumberFeature cucumberFeature) {
        resultListener.startFeature();
        reporter.uri(cucumberFeature.getPath());
        runtime.runFeature(cucumberFeature);

        if (!resultListener.isPassed()) {
            throw new CucumberException(resultListener.getFirstError());
        }
    }

    public void runScenario(PickleEvent pickle) throws Throwable {
        testCaseResultListener.startPickle();
        runtime.getRunner().runPickle(pickle);

        if (!testCaseResultListener.isPassed()) {
            throw testCaseResultListener.getError();
        }
    }

    public void finish() {
        runtime.getEventBus().send(new TestRunFinished(runtime.getEventBus().getTime()));
        runtime.printSummary();
    }

    /**
     * @return List of detected cucumber features
     */
    public List<CucumberFeature> getFeatures() {
        return runtimeOptions.cucumberFeatures(resourceLoader, runtime.getEventBus());
    }

    /**
     * @return returns the cucumber features as a two dimensional array of
     * {@link CucumberFeatureWrapper} objects.
     */
    public Object[][] provideFeatures() {
        System.err.println("WARNING: Mapping Cucumber Features to TestNG test is deprecated. Please use TestNGCucumberRunner.providePickleEvent as data provider.");
        try {
            List<CucumberFeature> features = getFeatures();
            List<Object[]> featuresList = new ArrayList<Object[]>(features.size());
            for (CucumberFeature feature : features) {
                featuresList.add(new Object[]{new CucumberFeatureWrapperImpl(feature)});
            }
            return featuresList.toArray(new Object[][]{});
        } catch (CucumberException e) {
            return new Object[][]{new Object[]{new CucumberExceptionWrapper(e)}};
        }
    }

    /**
     * @return returns the cucumber scenarios as a two dimensional array of {@link PickleEventWrapper}
     * scenarios combined with their {@link CucumberFeatureWrapper} feature.
     */
    public Object[][] provideScenarios() {
        try {
            List<Object[]> scenarios = new ArrayList<Object[]>();

            List<CucumberFeature> features = getFeatures();
            for (CucumberFeature feature : features) {
                List<PickleEvent> pickles = runtime.compileFeature(feature);

                for (PickleEvent pickle : pickles) {
                    if (runtime.matchesFilters(pickle)) {
                        scenarios.add(new Object[]{new PickleEventWrapper(pickle),
                            new CucumberFeatureWrapperImpl(feature)});
                    }
                }
            }
            return scenarios.toArray(new Object[][]{});
        } catch (CucumberException e) {
            return new Object[][]{new Object[]{new CucumberExceptionWrapper(e)}};
        }
    }
}
