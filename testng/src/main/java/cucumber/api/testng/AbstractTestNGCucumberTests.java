package cucumber.api.testng;

import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.junit.RuntimeOptionsFactory;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestResult;
import org.testng.annotations.Test;

public abstract class AbstractTestNGCucumberTests implements IHookable {
    private TestNgReporter reporter;
    private Runtime runtime;
    private ClassLoader classLoader;
    private ResourceLoader resourceLoader;

    public AbstractTestNGCucumberTests() {
        classLoader = getClass().getClassLoader();
        resourceLoader = new MultiLoader(classLoader);

        RuntimeOptionsFactory runtimeOptionsFactory = new RuntimeOptionsFactory(getClass());
        RuntimeOptions runtimeOptions = runtimeOptionsFactory.create();

        reporter = new TestNgReporter(System.out);
        runtimeOptions.formatters.add(reporter);
        runtime = new Runtime(resourceLoader, classLoader, runtimeOptions);
    }

    @Test(groups = "cucumber", description = "Runs Cucumber Features")
    public void run_cukes() {
        runtime.run();

        if (!runtime.getErrors().isEmpty()) {
            throw new RuntimeException(runtime.getErrors().get(0));
        }
    }

    @Override
    public void run(IHookCallBack iHookCallBack, ITestResult iTestResult) {
        iHookCallBack.runTestMethod(iTestResult);
    }

}
