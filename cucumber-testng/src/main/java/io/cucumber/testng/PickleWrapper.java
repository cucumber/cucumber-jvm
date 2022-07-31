package io.cucumber.testng;

import org.apiguardian.api.API;

/**
 * The only purpose of this interface is to be able to provide a custom string
 * representation, making TestNG reports look more descriptive.
 *
 * @see AbstractTestNGCucumberTests#runScenario(PickleWrapper, FeatureWrapper)
 */
@API(status = API.Status.STABLE)
public interface PickleWrapper {

    Pickle getPickle();

}
