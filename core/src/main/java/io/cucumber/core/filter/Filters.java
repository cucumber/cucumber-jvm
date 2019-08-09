package io.cucumber.core.filter;

import gherkin.events.PickleEvent;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class Filters {

    private final List<Predicate<PickleEvent>> filters;

    private int count;

    public Filters(Options options) {
        filters = new ArrayList<>();
        List<String> tagExpressions = options.getTagExpressions();
        if (!tagExpressions.isEmpty()) {
            this.filters.add(new TagPredicate(tagExpressions));
        }
        List<Pattern> nameFilters = options.getNameFilters();
        if (!nameFilters.isEmpty()) {
            this.filters.add(new NamePredicate(nameFilters));
        }
        Map<URI, ? extends Collection<Integer>> lineFilters = options.getLineFilters();
        if (!lineFilters.isEmpty()) {
            this.filters.add(new LinePredicate(lineFilters));
        }

        this.count = options.getLimitCount();
    }

    public boolean matchesFilters(PickleEvent pickleEvent) {
        for (Predicate<PickleEvent> filter : filters) {
            if (!filter.test(pickleEvent)) {
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
