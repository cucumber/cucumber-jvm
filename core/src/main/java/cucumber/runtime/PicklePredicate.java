package cucumber.runtime;

import gherkin.events.PickleEvent;

public interface PicklePredicate {

    boolean apply(PickleEvent pickleEvent);
}
