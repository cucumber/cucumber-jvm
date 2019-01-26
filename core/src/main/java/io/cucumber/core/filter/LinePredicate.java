package io.cucumber.core.filter;

import gherkin.events.PickleEvent;
import gherkin.pickles.PickleLocation;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

class LinePredicate implements PicklePredicate {
    private Map<URI, ? extends Collection<Integer>> lineFilters;

    LinePredicate(Map<URI, ? extends Collection<Integer>> lineFilters) {
        this.lineFilters = lineFilters;
    }

    @Override
    public boolean apply(PickleEvent pickleEvent) {
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
