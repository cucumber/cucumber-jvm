package cucumber.api.testng;

import gherkin.events.PickleEvent;

/**
 * The only purpose of this class is to provide custom {@linkplain #toString()},
 * making TestNG reports look more descriptive.
 *
 * @see AbstractTestNGCucumberTests#runScenario(cucumber.api.testng.PickleEventWrapper, cucumber.api.testng.CucumberFeatureWrapper)
 */
public class PickleEventWrapperImpl implements PickleEventWrapper {

    private final PickleEvent pickleEvent;

    PickleEventWrapperImpl(PickleEvent pickleEvent) {
        this.pickleEvent = pickleEvent;
    }

    public PickleEvent getPickleEvent() {
        return pickleEvent;
    }

    @Override
    public String toString() {
        return "\"" + pickleEvent.pickle.getName() + "\"";
    }
}
