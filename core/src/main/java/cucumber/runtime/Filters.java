package cucumber.runtime;

import cucumber.runtime.io.ResourceLoader;
import gherkin.events.PickleEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Filters {

    private final List<PicklePredicate> filters;
    private final RuntimeOptions runtimeOptions;

    public Filters(RuntimeOptions runtimeOptions, ResourceLoader resourceLoader) {
        this.runtimeOptions = runtimeOptions;

        filters = new ArrayList<PicklePredicate>();
        List<String> tagFilters = this.runtimeOptions.getTagFilters();
        if (!tagFilters.isEmpty()) {
            this.filters.add(new TagPredicate(tagFilters));
        }
        List<Pattern> nameFilters = runtimeOptions.getNameFilters();
        if (!nameFilters.isEmpty()) {
            this.filters.add(new NamePredicate(nameFilters));
        }
        Map<String, List<Long>> lineFilters = runtimeOptions.getLineFilters(resourceLoader);
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
