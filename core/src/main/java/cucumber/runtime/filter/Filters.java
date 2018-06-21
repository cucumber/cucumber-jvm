package cucumber.runtime.filter;

import cucumber.messages.Pickles.Pickle;
import cucumber.runtime.RuntimeOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Filters {

    private final List<PicklePredicate> filters;

    public Filters(RuntimeOptions runtimeOptions, RerunFilters rerunFilters) {
        filters = new ArrayList<>();
        List<String> tagFilters = runtimeOptions.getTagFilters();
        if (!tagFilters.isEmpty()) {
            this.filters.add(new TagPredicate(tagFilters));
        }
        List<Pattern> nameFilters = runtimeOptions.getNameFilters();
        if (!nameFilters.isEmpty()) {
            this.filters.add(new NamePredicate(nameFilters));
        }
        Map<String, List<Long>> lineFilters = runtimeOptions.getLineFilters();
        Map<String, List<Long>> rerunlineFilters = rerunFilters.processRerunFiles();
        for (Map.Entry<String, List<Long>> line : rerunlineFilters.entrySet()) {
            addLineFilters(lineFilters, line.getKey(), line.getValue());
        }
        if (!lineFilters.isEmpty()) {
            this.filters.add(new LinePredicate(lineFilters));
        }
    }

    public boolean matchesFilters(Pickle pickle) {
        for (PicklePredicate filter : filters) {
            if (!filter.apply(pickle)) {
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
