package io.cucumber.core.filter;

import gherkin.events.PickleEvent;
import gherkin.pickles.PickleLocation;

import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;

final class LinePredicate implements Predicate<PickleEvent> {
    private final Map<URI, ? extends Collection<Integer>> lineFilters;

    LinePredicate(Map<URI, ? extends Collection<Integer>> lineFilters) {
        this.lineFilters = lineFilters;
    }

    @Override
    public boolean test(PickleEvent pickleEvent) {
        URI picklePath = URI.create(pickleEvent.uri);
        if (!lineFilters.containsKey(picklePath)) {
            return true;
        }
        for (Integer line : lineFilters.get(picklePath)) {
            for (PickleLocation location : pickleEvent.pickle.getLocations()) {
                if (line == location.getLine()) {
                    return true;
                }
            }
        }
        return false;
    }
}
