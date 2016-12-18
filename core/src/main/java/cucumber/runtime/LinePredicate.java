package cucumber.runtime;

import gherkin.events.PickleEvent;
import gherkin.pickles.PickleLocation;

import java.util.List;
import java.util.Map;

public class LinePredicate implements PicklePredicate {
    private Map<String, List<Long>> lineFilters;

    public LinePredicate(Map<String, List<Long>> lineFilters) {
        this.lineFilters = lineFilters;
    }

    @Override
    public boolean apply(PickleEvent pickleEvent) {
        String picklePath = pickleEvent.uri;
        if (!lineFilters.containsKey(picklePath)) {
            return true;
        }
        for (Long line : lineFilters.get(picklePath)) {
            for (PickleLocation location : pickleEvent.pickle.getLocations()) {
                if (line == location.getLine()) {
                    return true;
                }
            }
        }
        return false;
    }
}
