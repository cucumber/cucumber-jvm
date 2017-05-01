package cucumber.api.testng;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Set;

@Test
public final class AbstractTestNGCucumberScenariosTest {

    private Set<String> invokedConfigurationMethodNames;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        InvokedConfigurationMethodListener icml = new InvokedConfigurationMethodListener();
        TestNG testNG = new TestNG();
        testNG.addListener(icml);
        testNG.setGroups("cucumber-scenarios");
        testNG.setTestClasses(new Class[]{new AbstractTestNGCucumberTests() {}.getClass()});
        testNG.run();
        invokedConfigurationMethodNames = icml.getInvokedMethodNames();
    }

    @Test
    public void setUpClassScenarioIsInvoked() {
        Assert.assertTrue(invokedConfigurationMethodNames.contains("setUpClass"), "setUpClass must be invoked");
    }

    @Test
    public void tearDownClassScenarioIsInvoked() {
        Assert.assertTrue(invokedConfigurationMethodNames.contains("tearDownClass"), "tearDownClass must be invoked");
    }
}
