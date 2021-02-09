package io.cucumber.custompredicateclass;

import io.cucumber.core.gherkin.Pickle;

import java.util.function.Predicate;

public class FilterOutPredicate implements Predicate<Pickle> {

    @Override
    public boolean test(Pickle pickle) {
        return !pickle.getName().toLowerCase().contains("break suite if not filtered");
    }

}
