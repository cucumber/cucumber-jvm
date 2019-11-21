package io.cucumber.core.filter;

import io.cucumber.core.feature.CucumberPickle;

import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;

final class LinePredicate implements Predicate<CucumberPickle> {
    private final Map<URI, ? extends Collection<Integer>> lineFilters;

    LinePredicate(Map<URI, ? extends Collection<Integer>> lineFilters) {
        this.lineFilters = lineFilters;
    }

    @Override
    public boolean test(CucumberPickle pickle) {
        URI picklePath = pickle.getUri();
        if (!lineFilters.containsKey(picklePath)) {
            return true;
        }
        for (Integer line : lineFilters.get(picklePath)) {
            if (line == pickle.getLocation().getLine() || line == pickle.getScenarioLocation().getLine()) {
                return true;
            }
        }
        return false;
    }
}
