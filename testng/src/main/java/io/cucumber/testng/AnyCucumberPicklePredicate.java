package io.cucumber.testng;

import java.util.function.Predicate;

/**
 * This predicate does nothing. It is solely needed for marking purposes.
 */
public final class AnyCucumberPicklePredicate implements Predicate<io.cucumber.core.gherkin.Pickle> {

    @Override
    public boolean test(io.cucumber.core.gherkin.Pickle pickle) {
        return true;
    }
}
