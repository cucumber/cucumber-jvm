package io.cucumber.testng;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.testng.Assert;
import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test
public final class AbstractTestNGCucumberTestsTest {

    private Set<String> invokedConfigurationMethodNames;
    private List<String> invokedTestMethodNames;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        InvokedMethodListener icml = new InvokedMethodListener();
        TestNG testNG = new TestNG();
        testNG.addListener((ITestNGListener)icml);
        testNG.setGroups("cucumber");
        testNG.setTestClasses(new Class[]{RunFeatureWithThreeScenariosTest.class});
        testNG.run();
        invokedConfigurationMethodNames = icml.getInvokedConfigurationMethodNames();
        invokedTestMethodNames = icml.getInvokedTestMethodNames();
    }
    
    @Test
    public void setUpClassIsInvoked() {
        Assert.assertTrue(invokedConfigurationMethodNames.contains("setUpClass"), "setUpClass must be invoked");
    }
    
    @Test
    public void tearDownClassIsInvoked() {
        Assert.assertTrue(invokedConfigurationMethodNames.contains("tearDownClass"), "tearDownClass must be invoked");
    }

    @Test
    public void runScenarioIsInvokedThreeTimes() {
        Assert.assertEquals(Collections.frequency(invokedTestMethodNames, "runScenario"), 3,
            "runScenario() must be invoked three times");
    }
}
