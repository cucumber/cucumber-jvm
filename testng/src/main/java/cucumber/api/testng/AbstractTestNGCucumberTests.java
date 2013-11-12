package cucumber.api.testng;

import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestResult;
import org.testng.annotations.Test;

import java.io.IOException;

public abstract class AbstractTestNGCucumberTests implements IHookable {

    @Test(groups = "cucumber", description = "Runs Cucumber Features")
    public void run_cukes() throws IOException {
        new TestNGCucumberRunner(getClass()).runCukes();
    }

    @Override
    public void run(IHookCallBack iHookCallBack, ITestResult iTestResult) {
        iHookCallBack.runTestMethod(iTestResult);
    }

}
