package io.cucumber.core.filter;

import io.cucumber.core.gherkin.Pickle;

import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

final class LinePredicate implements Predicate<Pickle> {

    private final Map<URI, ? extends Collection<Integer>> lineFilters;

    LinePredicate(Map<URI, ? extends Collection<Integer>> lineFilters) {
        this.lineFilters = lineFilters;
    }

    @Override
    public boolean test(Pickle pickle) {
        URI picklePath = pickle.getUri();
        if (!lineFilters.containsKey(picklePath)) {
            return true;
        }
        for (Integer line : lineFilters.get(picklePath)) {
            if (Objects.equals(line, pickle.getLocation().getLine())
                    || Objects.equals(line, pickle.getScenarioLocation().getLine())) {
                return true;
            }
        }
        return false;
    }

}
