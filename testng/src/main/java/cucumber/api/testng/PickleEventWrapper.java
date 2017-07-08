package cucumber.api.testng;

import gherkin.events.PickleEvent;

public class PickleEventWrapper {

    private final PickleEvent pickleEvent;

    PickleEventWrapper(PickleEvent pickleEvent) {
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
