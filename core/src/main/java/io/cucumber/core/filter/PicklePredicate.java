package io.cucumber.core.filter;

import gherkin.events.PickleEvent;

interface PicklePredicate {

    boolean apply(PickleEvent pickleEvent);
}
