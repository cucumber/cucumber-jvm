package cucumber.runtime.filter;

import io.cucumber.messages.Messages.Pickle;

interface PicklePredicate {

    boolean apply(Pickle pickle);
}
