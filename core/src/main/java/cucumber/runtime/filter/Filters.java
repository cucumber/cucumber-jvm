package cucumber.runtime.filter;

import gherkin.events.PickleEvent;
import io.cucumber.core.options.FilterOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Filters {

    private final List<PicklePredicate> filters;

    public Filters(FilterOptions filterOPtions) {

        filters = new ArrayList<>();
        List<String> tagFilters = filterOPtions.getTagFilters();
        if (!tagFilters.isEmpty()) {
            this.filters.add(new TagPredicate(tagFilters));
        }
        List<Pattern> nameFilters = filterOPtions.getNameFilters();
        if (!nameFilters.isEmpty()) {
            this.filters.add(new NamePredicate(nameFilters));
        }
        Map<String, List<Long>> lineFilters = filterOPtions.getLineFilters();
        if (!lineFilters.isEmpty()) {
            this.filters.add(new LinePredicate(lineFilters));
        }
    }

    public boolean matchesFilters(PickleEvent pickleEvent) {
        for (PicklePredicate filter : filters) {
            if (!filter.apply(pickleEvent)) {
                return false;
            }
        }
        return true;
    }

}
