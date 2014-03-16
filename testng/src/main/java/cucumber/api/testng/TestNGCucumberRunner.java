package cucumber.api.testng;

import cucumber.api.CucumberOptions;
import cucumber.runtime.ClassFinder;
import cucumber.runtime.CucumberException;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.RuntimeOptionsFactory;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Glue code for running Cucumber via TestNG.
 */
public class TestNGCucumberRunner {

    private final cucumber.runtime.Runtime runtime;

    /**
     * Bootstrap the cucumber runtime
     *
     * @param clazz Which has the cucumber.api.CucumberOptions and org.testng.annotations.Test annotations
     */
    public TestNGCucumberRunner(Class clazz) {
        ClassLoader classLoader = clazz.getClassLoader();
        ResourceLoader resourceLoader = new MultiLoader(classLoader);

        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(clazz, new Class[]{CucumberOptions.class});
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();

        // remove duplicates from glue path.
        List<String> uniqueGlue = new ArrayList<String>(new HashSet<String>(runtimeOptions.getGlue()));
        runtimeOptions.getGlue().clear();
        runtimeOptions.getGlue().addAll(uniqueGlue);

        TestNgReporter reporter = new TestNgReporter(System.out);
        runtimeOptions.addFormatter(reporter);
        ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        runtime = new cucumber.runtime.Runtime(resourceLoader, classFinder, classLoader, runtimeOptions);
    }

    /**
     * Run the Cucumber features
     */
    public void runCukes() {
        try {
            runtime.run();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        runtime.printSummary();
        if (!runtime.getErrors().isEmpty()) {
            throw new CucumberException(runtime.getErrors().get(0));
        }
    }

}
