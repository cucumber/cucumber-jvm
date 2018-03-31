package cucumber.api.testng;

import gherkin.events.PickleEvent;

/**
 * The only purpose of this interface is to be able to provide a custom
 * <pre>toString()</pre>, making TestNG reports look more descriptive.
 *
 * @see PickleEventWrapperImpl
 */
public interface PickleEventWrapper {
    public PickleEvent getPickleEvent();
}
