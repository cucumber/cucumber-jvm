package io.cucumber.testng;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.frequency;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Test
public final class AbstractTestNGCucumberTestsTest {

    private final InvokedMethodListener listener = new InvokedMethodListener();

    @BeforeClass(alwaysRun = true)
    public void setUp() {

        TestNG testNG = new TestNG();
        testNG.addListener(listener);
        testNG.setGroups("cucumber");
        testNG.setTestClasses(new Class[] { RunFeatureWithThreeScenariosTest.class });
        testNG.run();
    }

    @Test
    public void setUpClassIsInvoked() {
        assertTrue(listener.getInvokedTestMethods().stream()
                .filter(IInvokedMethod::isConfigurationMethod)
                .map(IInvokedMethod::getTestMethod)
                .map(ITestNGMethod::getMethodName)
                .anyMatch("setUpClass"::equals),
            "setUpClass() must be invoked");
    }

    @Test
    public void tearDownClassIsInvoked() {
        assertTrue(listener.getInvokedTestMethods().stream()
                .filter(IInvokedMethod::isConfigurationMethod)
                .map(IInvokedMethod::getTestMethod)
                .map(ITestNGMethod::getMethodName)
                .anyMatch("tearDownClass"::equals),
            "tearDownClass() must be invoked");
    }

    @Test
    public void runScenarioIsInvokedThreeTimes() {
        List<String> invokedTestMethodNames = listener.getInvokedTestMethods().stream()
                .filter(IInvokedMethod::isTestMethod)
                .map(IInvokedMethod::getTestMethod)
                .map(ITestNGMethod::getMethodName)
                .collect(Collectors.toList());

        assertEquals(frequency(invokedTestMethodNames, "runScenario"), 3,
            "runScenario() must be invoked three times");
    }

    @Test
    public void providesPickleWrapperAsFirstArgumentWithQuotedStringRepresentation() {
        List<String> scenarioNames = listener.getInvokedTestMethods().stream()
                .filter(IInvokedMethod::isTestMethod)
                .map(IInvokedMethod::getTestResult)
                .map(ITestResult::getParameters)
                .map(objects -> objects[0])
                .map(o -> (PickleWrapper) o)
                .map(Objects::toString)
                .collect(Collectors.toList());

        assertEquals(scenarioNames, asList("\"SC1\"", "\"SC2\"", "\"SC3\""));
    }

    @Test
    public void providesFeatureWrapperAsSecondArgumentWithQuotedStringRepresentation() {
        List<String> featureNames = listener.getInvokedTestMethods().stream()
                .filter(IInvokedMethod::isTestMethod)
                .map(IInvokedMethod::getTestResult)
                .map(ITestResult::getParameters)
                .map(objects -> objects[1])
                .map(o -> (FeatureWrapper) o)
                .map(Objects::toString)
                .collect(Collectors.toList());

        assertEquals(frequency(featureNames, "\"A feature containing 3 scenarios\""), 3);
    }

    private static final class InvokedMethodListener implements IInvokedMethodListener {

        private final List<IInvokedMethod> invokedTestMethods = new ArrayList<>();

        @Override
        public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        }

        @Override
        public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
            invokedTestMethods.add(method);
        }

        public List<IInvokedMethod> getInvokedTestMethods() {
            return invokedTestMethods;
        }
    }
}
