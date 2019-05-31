package cucumber.runtime.filter;

import gherkin.events.PickleEvent;
import io.cucumber.core.options.FilterOptions;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Filters {

    private final List<PicklePredicate> filters;
    
    private int count;

    public Filters(FilterOptions filterOptions) {

        filters = new ArrayList<>();
        List<String> tagFilters = filterOptions.getTagFilters();
        if (!tagFilters.isEmpty()) {
            this.filters.add(new TagPredicate(tagFilters));
        }
        List<Pattern> nameFilters = filterOptions.getNameFilters();
        if (!nameFilters.isEmpty()) {
            this.filters.add(new NamePredicate(nameFilters));
        }
        Map<URI, ? extends Collection<Integer>> lineFilters = filterOptions.getLineFilters();
        if (!lineFilters.isEmpty()) {
            this.filters.add(new LinePredicate(lineFilters));
        }
        
        this.count = filterOptions.getLimitCount(); 
    }

    public boolean matchesFilters(PickleEvent pickleEvent) {
        for (PicklePredicate filter : filters) {
            if (!filter.apply(pickleEvent)) {
                return false;
            }
        }
        return true;
    }
    
    public List<PickleEvent> limitPickleEvents(List<PickleEvent> pickleEvents) {
    	if (count > pickleEvents.size() || count < 1) {
    		return pickleEvents;
    	}
		return pickleEvents.subList(0, count);
	}
}
