package cucumber.runtime.filter;

import cucumber.messages.Pickles.Pickle;
import cucumber.messages.Sources.Location;

import java.util.List;
import java.util.Map;

class LinePredicate implements PicklePredicate {
    private Map<String, List<Long>> lineFilters;

    LinePredicate(Map<String, List<Long>> lineFilters) {
        this.lineFilters = lineFilters;
    }

    @Override
    public boolean apply(Pickle pickle) {
        String picklePath = pickle.getUri();
        if (!lineFilters.containsKey(picklePath)) {
            return true;
        }
        for (Long line : lineFilters.get(picklePath)) {
            for (Location location : pickle.getLocationsList()) {
                if (line == location.getLine()) {
                    return true;
                }
            }
        }
        return false;
    }
}
