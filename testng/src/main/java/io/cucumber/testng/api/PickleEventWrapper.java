package io.cucumber.testng.api;

import gherkin.events.PickleEvent;

/**
 * The only purpose of this interface is to be able to provide a custom
 * <pre>toString()</pre>, making TestNG reports look more descriptive.
 *
 * @see AbstractTestNGCucumberTests#runScenario(PickleEventWrapper, CucumberFeatureWrapper)
 */
public interface PickleEventWrapper {

    PickleEvent getPickleEvent();

}
