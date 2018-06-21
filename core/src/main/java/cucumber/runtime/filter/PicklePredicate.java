package cucumber.runtime.filter;

import cucumber.messages.Pickles.Pickle;

interface PicklePredicate {

    boolean apply(Pickle pickle);
}
