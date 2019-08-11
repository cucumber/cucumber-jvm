package io.cucumber.core.filter;

import gherkin.events.PickleEvent;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

final class NamePredicate implements Predicate<PickleEvent> {
    private final List<Pattern> patterns;

    NamePredicate(List<Pattern> patterns) {
        this.patterns = patterns;
    }

    @Override
    public boolean test(PickleEvent pickleEvent) {
        String name = pickleEvent.pickle.getName();
        for (Pattern pattern : patterns) {
            if (pattern.matcher(name).find()) {
                return true;
            }
        }
        return false;
    }

}
