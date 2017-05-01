package cucumber.api.testng;

import cucumber.runtime.model.CucumberTagStatement;

/**
 * The only purpose of this interface is to be able to provide a custom
 * <pre>toString()</pre>, making TestNG reports look more descriptive.
 *
 * @see CucumberFeatureWrapperImpl
 */
public interface CucumberTagStatementWrapper {
    CucumberTagStatement getCucumberScenario();
}
