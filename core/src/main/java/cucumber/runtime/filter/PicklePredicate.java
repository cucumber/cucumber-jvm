package cucumber.runtime.filter;

import gherkin.events.PickleEvent;

interface PicklePredicate {

    boolean apply(PickleEvent pickleEvent);
}
