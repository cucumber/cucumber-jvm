package io.cucumber.testng;

import org.apiguardian.api.API;

/**
 * The only purpose of this interface is to be able to provide a custom
 * <pre>toString()</pre>, making TestNG reports look more descriptive.
 *
 * @see AbstractTestNGCucumberTests#runScenario(PickleEventWrapper, CucumberFeatureWrapper)
 */
@API(status = API.Status.STABLE)
public interface CucumberFeatureWrapper {

}
