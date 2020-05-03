package io.cucumber.core.filter;

import io.cucumber.core.gherkin.Pickle;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

final class NamePredicate implements Predicate<Pickle> {

    private final List<Pattern> patterns;

    NamePredicate(List<Pattern> patterns) {
        this.patterns = patterns;
    }

    @Override
    public boolean test(Pickle pickle) {
        String name = pickle.getName();
        return patterns.stream().anyMatch(pattern -> pattern.matcher(name).find());
    }

}
