package io.cucumber.core.filter;

import gherkin.events.PickleEvent;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class Filters implements Predicate<PickleEvent> {

    private Predicate<PickleEvent> filter;

    private int count;

    public Filters(Options options) {
        this.filter = t -> true;
        List<String> tagExpressions = options.getTagExpressions();
        if (!tagExpressions.isEmpty()) {
            this.filter = this.filter.and(new TagPredicate(tagExpressions));
        }
        List<Pattern> nameFilters = options.getNameFilters();
        if (!nameFilters.isEmpty()) {
            this.filter = this.filter.and(new NamePredicate(nameFilters));
        }
        Map<URI, ? extends Collection<Integer>> lineFilters = options.getLineFilters();
        if (!lineFilters.isEmpty()) {
            this.filter = this.filter.and(new LinePredicate(lineFilters));
        }

        this.count = options.getLimitCount();
    }

    @Override
    public boolean test(PickleEvent pickleEvent) {
        return this.filter.test(pickleEvent);
    }

    public List<PickleEvent> limitPickleEvents(List<PickleEvent> pickleEvents) {
        if (count > pickleEvents.size() || count < 1) {
            return pickleEvents;
        }
        return pickleEvents.subList(0, count);
    }
}
