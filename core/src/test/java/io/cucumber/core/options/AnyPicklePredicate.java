package io.cucumber.core.options;

import io.cucumber.core.gherkin.Pickle;

import java.util.function.Predicate;

/**
 * This predicate does nothing. It is solely needed for marking purposes.
 */
public final class AnyPicklePredicate implements Predicate<Pickle> {

    @Override
    public boolean test(Pickle pickle) {
        return true;
    }
}
