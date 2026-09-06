package io.cucumber.testng;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.util.SetSystemProperty;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.internal.RuntimeBehavior;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

final class RunWithTestNgTest {

    private final InvokedMethodListener listener = new InvokedMethodListener();

    private void run(Class<?> clazz) {
        TestNG testNG = new TestNG();
        testNG.addListener(listener);
        testNG.setGroups("cucumber");
        testNG.setTestClasses(new Class[] { clazz });
        testNG.run();
    }

    @Test
    void setUpClassIsInvoked() {
        run(RunFeatureWithThreeScenariosTest.class);

        var events = listener.getInvokedTestMethods().stream()
                .filter(IInvokedMethod::isConfigurationMethod)
                .map(IInvokedMethod::getTestMethod)
                .map(ITestNGMethod::getMethodName)
                .toList();
        assertThat(events).contains("setUpClass");
    }

    @Test
    void tearDownClassIsInvoked() {
        run(RunFeatureWithThreeScenariosTest.class);

        var events = listener.getInvokedTestMethods().stream()
                .filter(IInvokedMethod::isConfigurationMethod)
                .map(IInvokedMethod::getTestMethod)
                .map(ITestNGMethod::getMethodName)
                .toList();
        assertThat(events).contains("tearDownClass");
    }

    @Test
    void runScenarioIsInvokedThreeTimes() {
        run(RunFeatureWithThreeScenariosTest.class);

        var events = listener.getInvokedTestMethods().stream()
                .filter(IInvokedMethod::isTestMethod)
                .map(IInvokedMethod::getTestMethod)
                .map(ITestNGMethod::getMethodName)
                .toList();

        assertThat(events).containsExactly("runScenario", "runScenario", "runScenario");
    }

    @Test
    void providesPickleWrapperAsFirstArgumentWithQuotedStringRepresentation() {
        run(RunFeatureWithThreeScenariosTest.class);

        var events = listener.getInvokedTestMethods().stream()
                .filter(IInvokedMethod::isTestMethod)
                .map(IInvokedMethod::getTestResult)
                .map(ITestResult::getParameters)
                .map(objects -> objects[0])
                .map(o -> (PickleWrapper) o)
                .map(Objects::toString)
                .toList();

        assertThat(events).containsExactly("\"SC1\"", "\"SC2\"", "\"SC3\"");
    }

    @Test
    void providesFeatureWrapperAsSecondArgumentWithQuotedStringRepresentation() {
        run(RunFeatureWithThreeScenariosTest.class);

        var events = listener.getInvokedTestMethods().stream()
                .filter(IInvokedMethod::isTestMethod)
                .map(IInvokedMethod::getTestResult)
                .map(ITestResult::getParameters)
                .map(objects -> objects[1])
                .map(o -> (FeatureWrapper) o)
                .map(Objects::toString)
                .toList();

        assertThat(events)
                .containsExactly(
                    "\"A feature containing 3 scenarios\"",
                    "\"A feature containing 3 scenarios\"",
                    "\"A feature containing 3 scenarios\"");
    }

    @Test
    @SetSystemProperty(key = RuntimeBehavior.TESTNG_MODE_DRYRUN, value = "true")
    void dryRun() {
        run(RunFeatureWithThreeScenariosTest.class);

        var events = listener.getInvokedTestMethods().stream()
                .filter(IInvokedMethod::isTestMethod)
                .map(IInvokedMethod::getTestResult)
                .map(ITestResult::getParameters)
                .map(objects -> objects[0])
                .map(o -> (PickleWrapper) o)
                .map(Objects::toString)
                .toList();

        assertThat(events).containsExactly("\"SC1\"", "\"SC2\"", "\"SC3\"");

        var pickles = listener.getInvokedTestMethods().stream()
                .filter(IInvokedMethod::isTestMethod)
                .map(IInvokedMethod::getTestResult)
                .map(ITestResult::getParameters)
                .map(objects -> objects[0])
                .map(o -> (PickleWrapper) o)
                .map(PickleWrapper::getPickle);

        assertThat(pickles)
                .extracting(Pickle::isDryRun)
                .allMatch(Boolean.TRUE::equals);
    }

    private static final class InvokedMethodListener implements IInvokedMethodListener {

        private final List<IInvokedMethod> invokedTestMethods = new ArrayList<>();

        @Override
        public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
            invokedTestMethods.add(method);
        }

        List<IInvokedMethod> getInvokedTestMethods() {
            return invokedTestMethods;
        }
    }
}
