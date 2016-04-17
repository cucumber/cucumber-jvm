package cucumber.api.testng;

import cucumber.runtime.*;
import cucumber.runtime.Runtime;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.Resource;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.runtime.model.CucumberFeature;
import gherkin.formatter.Formatter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Glue code for running Cucumber via TestNG.
 */
public class TestNGOneCucumberFeatureRunner {
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
    public TestNGOneCucumberFeatureRunner(Class clazz) {
        classLoader = clazz.getClassLoader();
        resourceLoader = new MultiLoader(classLoader);

        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(clazz);
        runtimeOptions = runtimeOptionsFactory.create();

        TestNgReporter reporter = new TestNgReporter(System.out);
        ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        resultListener = new FeatureResultListener(runtimeOptions.reporter(classLoader), runtimeOptions.isStrict());
        runtime = new Runtime(resourceLoader, classFinder, classLoader, runtimeOptions);
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

    public void finish() {
        Formatter formatter = runtimeOptions.formatter(classLoader);

        formatter.done();
        formatter.close();
        runtime.printSummary();
    }

    /**
     * @return List of detected cucumber features
     */
    // Convert to Iterator<Object[]>
    public Iterator<Object[]> getFeatureResources() {
        List<Resource> featureResourceList = runtimeOptions.getCucumberFeaturesAsResources(resourceLoader);
        List<Object[]> featureResources = new ArrayList<Object[]>(featureResourceList.size());
        for (Resource resource : featureResourceList) {
            featureResources.add(new Object[] {resource});
        }
        return featureResources.iterator();
    }
}
