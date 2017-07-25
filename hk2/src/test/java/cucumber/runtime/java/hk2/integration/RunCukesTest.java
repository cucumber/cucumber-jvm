package cucumber.runtime.java.hk2.integration;

import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 * The Cucumber integration tests use a mixture of annotation and module binding to demostrate both techniques.
 * The step definition classes are all bound in scenario scope using the @ScenarioScoped annotation.
 * The test object classes are bound in the TestBinder class.
 */
@RunWith(Cucumber.class)
public class RunCukesTest {
}
