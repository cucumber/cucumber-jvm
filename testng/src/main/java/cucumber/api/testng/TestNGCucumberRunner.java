package cucumber.api.testng;

import cucumber.runtime.ClassFinder;
import cucumber.runtime.CucumberException;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.RuntimeOptionsFactory;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberTagStatement;
import cucumber.runtime.model.CucumberScenarioOutline;
import cucumber.runtime.model.CucumberExamples;
import cucumber.runtime.model.CucumberScenario;
import gherkin.formatter.Formatter;

import java.util.ArrayList;
import java.util.List;

/**
 * Glue code for running Cucumber via TestNG.
 */
public class TestNGCucumberRunner {
    private Runtime runtime;
    private RuntimeOptions runtimeOptions;
    private ResourceLoader resourceLoader;
    private FeatureResultListener resultListener;
    private ClassLoader classLoader;

    /**
     * Bootstrap the cucumber runtime
     *
     * @param clazz Which has the cucumber.api.CucumberOptions and org.testng.annotations.Test annotations
     */
    public TestNGCucumberRunner(Class clazz) {
        classLoader = clazz.getClassLoader();
        resourceLoader = new MultiLoader(classLoader);

        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(clazz);
        runtimeOptions = runtimeOptionsFactory.create();

        TestNgReporter reporter = new TestNgReporter(System.out);
        ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        resultListener = new FeatureResultListener(runtimeOptions.reporter(classLoader), runtimeOptions.isStrict());
        runtime = new Runtime(resourceLoader, classFinder, classLoader, runtimeOptions);
    }

    /**
     * Run the Cucumber features
     */
    public void runCukes() {
        for (CucumberFeature cucumberFeature : getFeatures()) {
            cucumberFeature.run(
                    runtimeOptions.formatter(classLoader),
                    resultListener,
                    runtime);
        }
        finish();
        if (!resultListener.isPassed()) {
            throw new CucumberException(resultListener.getFirstError());
        }
    }

    public void runCucumber(CucumberFeature cucumberFeature) {
        resultListener.startFeature();
        cucumberFeature.run(
                runtimeOptions.formatter(classLoader),
                resultListener,
                runtime);

        if (!resultListener.isPassed()) {
            throw new CucumberException(resultListener.getFirstError());
        }
    }

    public void runCucumberScenario(CucumberTagStatement cucumberTagStatement) throws Throwable {
        resultListener.startFeature();
        cucumberTagStatement.run(
                runtimeOptions.formatter(classLoader),
                resultListener,
                runtime);

        if (!resultListener.isPassed()) {
            throw resultListener.getFirstError();
        }
    }

    public void finish() {
        Formatter formatter = runtimeOptions.formatter(classLoader);

        formatter.done();
        formatter.close();
        runtime.printSummary();
    }

    /**
     * @return List of detected cucumber features
     */
    public List<CucumberFeature> getFeatures() {
        return runtimeOptions.cucumberFeatures(resourceLoader);
    }

    /**
     * @return List of detected cucumber scenarios
     */
    public List<CucumberTagStatement> getScenarios() {
        List<CucumberTagStatement> scenarios = new ArrayList<CucumberTagStatement>();

        List<CucumberFeature> features = getFeatures();
        for (CucumberFeature feature: features) {
            List<CucumberTagStatement> featureScenarios = feature.getFeatureElements();
            scenarios.addAll(featureScenarios);
        }

        return scenarios;
    }

    /**
     * @return returns the cucumber features as a two dimensional array of
     * {@link CucumberFeatureWrapper} objects.
     */
    public Object[][] provideFeatures() {
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
     * @return returns the Cucumber Scenarios as a two dimensional array of
     * {@link CucumberTagStatement} objects and scenario name.
     */
    public Object[][] provideScenarios() {
        try {
            List<CucumberFeature> features = getFeatures();
            List<Object[]> scenarioList = new ArrayList<Object[]>(features.size());

            for (CucumberFeature feature : features) {
                List<CucumberTagStatement> scenarios = feature.getFeatureElements();

                for (CucumberTagStatement scenario: scenarios) {
                    // If this is a Scenario Outline, split it up so each one is a test.
                    if (scenario instanceof CucumberScenarioOutline) {
                        List<CucumberExamples> cucumberExamplesList = ((CucumberScenarioOutline) scenario).getCucumberExamplesList();

                        for (CucumberExamples cucumberExamples : cucumberExamplesList) {
                            List<CucumberScenario> exampleScenarios = cucumberExamples.createExampleScenarios();
                            for (CucumberScenario exampleScenario : exampleScenarios) {
                                scenarioList.add(new Object[]{new CucumberTagStatementWrapperImpl(exampleScenario)});
                            }
                        }

                    }
                    else
                        scenarioList.add(new Object[]{new CucumberTagStatementWrapperImpl(scenario)});
                }

            }
            return scenarioList.toArray(new Object[][]{});
        } catch (CucumberException e) {
            return new Object[][]{new Object[]{new CucumberExceptionWrapper(e)}};
        }
    }

}
