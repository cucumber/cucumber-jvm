package cucumber.runtime.filter;

import gherkin.events.PickleEvent;
import io.cucumber.core.options.FilterOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Filters {

    private final List<PicklePredicate> filters;
    private final FilterOptions runtimeOptions;
    private final RerunFilters rerunFilters;

    public Filters(FilterOptions filterOPtions, RerunFilters rerunFilters) {
        this.runtimeOptions = filterOPtions;
        this.rerunFilters = rerunFilters;

        filters = new ArrayList<PicklePredicate>();
        List<String> tagFilters = this.runtimeOptions.getTagFilters();
        if (!tagFilters.isEmpty()) {
            this.filters.add(new TagPredicate(tagFilters));
        }
        List<Pattern> nameFilters = filterOPtions.getNameFilters();
        if (!nameFilters.isEmpty()) {
            this.filters.add(new NamePredicate(nameFilters));
        }
        Map<String, List<Long>> lineFilters = filterOPtions.getLineFilters();
        Map<String, List<Long>> rerunlineFilters = rerunFilters.processRerunFiles();
        for (Map.Entry<String,List<Long>> line: rerunlineFilters.entrySet()) {
            addLineFilters(lineFilters, line.getKey(), line.getValue());
        }
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

    private void addLineFilters(Map<String, List<Long>> parsedLineFilters, String key, List<Long> lines) {
        if (parsedLineFilters.containsKey(key)) {
            parsedLineFilters.get(key).addAll(lines);
        } else {
            parsedLineFilters.put(key, lines);
        }
    }

}
