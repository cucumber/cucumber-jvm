package cucumber.runtime;

import gherkin.events.PickleEvent;

interface PicklePredicate {

    boolean apply(PickleEvent pickleEvent);
}
