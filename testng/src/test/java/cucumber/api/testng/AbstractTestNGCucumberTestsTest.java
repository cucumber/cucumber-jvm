package cucumber.api.testng;

import java.util.Set;

import org.testng.Assert;
import org.testng.TestNG;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test
public final class AbstractTestNGCucumberTestsTest {

    private Set<String> invokedConfigurationMethodNames;

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        InvokedConfigurationMethodListener icml = new InvokedConfigurationMethodListener();
        TestNG testNG = new TestNG();
        testNG.addListener(icml);
        testNG.setGroups("cucumber");
        testNG.setTestClasses(new Class[]{new AbstractTestNGCucumberTests() {}.getClass()});
        testNG.run();
        invokedConfigurationMethodNames = icml.getInvokedMethodNames();
    }
    
    @Test
    public void setUpClassIsInvoked() {
        Assert.assertTrue(invokedConfigurationMethodNames.contains("setUpClass"), "setUpClass must be invoked");
    }
    
    @Test
    public void tearDownClassIsInvoked() {
        Assert.assertTrue(invokedConfigurationMethodNames.contains("tearDownClass"), "tearDownClass must be invoked");
    }
}
