package io.cucumber.testng;

import org.testng.Assert;
import org.testng.IDataProviderInterceptor;
import org.testng.IDataProviderMethod;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.TestNG;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import io.cucumber.core.gherkin.FeatureParserException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;

public class TestNGCucumberRunnerTest {

    private TestNGCucumberRunner testNGCucumberRunner;

    @Test
    public void runCucumberTest() throws Throwable {
        testNGCucumberRunner = new TestNGCucumberRunner(RunCucumberTest.class);

        for (Object[] scenario : testNGCucumberRunner.provideScenarios()) {
            PickleWrapper wrapper = (PickleWrapper) scenario[0];
            testNGCucumberRunner.runScenario(wrapper.getPickle());
        }
    }

    @Test
    public void runScenarioWithUndefinedStepsStrict() {
        testNGCucumberRunner = new TestNGCucumberRunner(RunScenarioWithUndefinedStepsStrict.class);
        Object[][] scenarios = testNGCucumberRunner.provideScenarios();

        // the feature file only contains one scenario
        assertEquals(scenarios.length, 1);
        Object[] scenario = scenarios[0];
        PickleWrapper wrapper = (PickleWrapper) scenario[0];

        assertThrows(
            UndefinedStepException.class,
            () -> testNGCucumberRunner.runScenario(wrapper.getPickle())
        );
    }

    @Test
    public void parse_error_propagated_to_testng_test_execution() {
        testNGCucumberRunner = new TestNGCucumberRunner(ParseError.class);
        try {
            Object[][] scenarios = testNGCucumberRunner.provideScenarios(); // a CucumberException is caught
            PickleWrapper pickleWrapper = (PickleWrapper) scenarios[0][0];
            pickleWrapper.getPickle();
            Assert.fail("CucumberException not thrown");
        } catch (FeatureParserException e) {
            assertEquals(e.getMessage(), "Failed to parse resource at: classpath:io/cucumber/error/parse-error.feature");
        }
    }

    @Test
    public void runScenariosWithAdditionalDataProvider() {
        InvokedMethodListener iml = new InvokedMethodListener();
        TestNG testNG = new TestNG();
        testNG.addListener(iml);
        testNG.addListener(new DataProviderInterceptor());
        testNG.setGroups("cucumber");
        testNG.setTestClasses(new Class[]{TestNGCucumberTestsWithExtraDataProvider.class});
        testNG.run();
        List<String> invokedConfigurationMethodNames = iml.getInvokedConfigurationMethodNames();
        List<String> invokedTestMethodNames = iml.getInvokedTestMethodNames();

        assertEquals(invokedConfigurationMethodNames.stream()
            .filter("setUpClass"::equals)
            .count(), 1L);
        assertEquals(invokedConfigurationMethodNames.stream()
            .filter("tearDownClass"::equals)
            .count(), 1L);
        assertEquals(invokedTestMethodNames.stream()
            .filter("runTag2Scenario"::equals)
            .count(), 3L);
        assertEquals(invokedTestMethodNames.stream()
            .filter("runScenario"::equals)
            .count(), 2L);
    }

    @CucumberOptions(
        features = "classpath:io/cucumber/undefined/undefined_steps.feature",
        strict = true
    )
    static class RunScenarioWithUndefinedStepsStrict extends AbstractTestNGCucumberTests {
    }

    @CucumberOptions(strict = true)
    static class RunCucumberTest extends AbstractTestNGCucumberTests {
    }

    @CucumberOptions(features = "classpath:io/cucumber/error/parse-error.feature")
    static class ParseError extends AbstractTestNGCucumberTests {
    }

    static class DataProviderInterceptor implements IDataProviderInterceptor {

        @Override
        public Iterator<Object[]> intercept(Iterator<Object[]> original, IDataProviderMethod dataProviderMethod, ITestNGMethod method, ITestContext iTestContext) {
            if ("tag2Scenarios".equals(dataProviderMethod.getName())) {
                return filterScenarios(original, tags -> tags.contains("@TAG2")).iterator();
            } else {
                return filterScenarios(original, tags -> tags.contains("@TAG1")).iterator();
            }
        }

        private Collection<Object[]> filterScenarios(Iterator<Object[]> originalDataSet, Predicate<Collection<String>> tagsCondition) {
            Collection<Object[]> filteredDataSet = new HashSet<>();
            originalDataSet.forEachRemaining(data -> Arrays.stream(data).forEach(item -> {
                if (item instanceof PickleWrapper) {
                    PickleWrapper pickleWrapper = (PickleWrapper) item;
                    if (tagsCondition.test(pickleWrapper.getPickle().getTags())) {
                        filteredDataSet.add(data);
                    }
                }
            }));
            return filteredDataSet;
        }
    }

    @CucumberOptions(
        features = "classpath:io/cucumber/testng/scenarios_with_tags.feature"
    )
    static class TestNGCucumberTestsWithExtraDataProvider extends AbstractTestNGCucumberTests {

        @Test(groups = "cucumber", description = "Runs Cucumber Scenarios", dataProvider = "tag2Scenarios")
        public void runTag2Scenario(PickleWrapper pickleWrapper, FeatureWrapper featureWrapper) throws Throwable {
            super.runScenario(pickleWrapper, featureWrapper);
        }

        @DataProvider
        public Object[][] tag2Scenarios() {
            return super.scenarios();
        }

    }
}
