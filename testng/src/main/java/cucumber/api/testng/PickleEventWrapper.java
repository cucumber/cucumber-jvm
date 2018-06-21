package cucumber.api.testng;

import cucumber.messages.Pickles.Pickle;

/**
 * The only purpose of this interface is to be able to provide a custom
 * <pre>toString()</pre>, making TestNG reports look more descriptive.
 *
 * @see AbstractTestNGCucumberTests#runScenario(cucumber.api.testng.PickleEventWrapper, cucumber.api.testng.CucumberFeatureWrapper)
 */
public interface PickleEventWrapper {

    Pickle getPickle();

}
