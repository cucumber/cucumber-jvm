package cucumber.cli;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class CucumberRunListener extends RunListener {
    @Override
    public void testRunStarted(Description description) throws Exception {
        System.out.println("testRunStarted");
    }

    @Override
    public void testRunFinished(Result result) throws Exception {
        System.out.println("testRunFinished");
    }

    @Override
    public void testStarted(Description description) throws Exception {
        System.out.println("testStarted");
    }

    @Override
    public void testFinished(Description description) throws Exception {
        System.out.println("testFinished");
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        System.out.println("testFailure");
    }

    @Override
    public void testAssumptionFailure(Failure failure) {
        System.out.println("testAssumptionFailure");
    }

    @Override
    public void testIgnored(Description description) throws Exception {
        System.out.println("testIgnored: " + description);
    }
}
