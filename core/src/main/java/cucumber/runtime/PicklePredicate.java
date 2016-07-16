package cucumber.runtime;

import gherkin.pickles.Pickle;

public interface PicklePredicate {

    boolean apply(Pickle pickle);
}
