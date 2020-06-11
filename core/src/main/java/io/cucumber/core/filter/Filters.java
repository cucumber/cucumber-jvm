package io.cucumber.core.filter;

import io.cucumber.core.gherkin.Pickle;
import io.cucumber.tagexpressions.Expression;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class Filters implements Predicate<Pickle> {

    private Predicate<Pickle> filter = t -> true;

    public Filters(Options options) {
        List<Expression> tagExpressions = options.getTagExpressions();
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
    }

    @Override
    public boolean test(Pickle pickle) {
        return this.filter.test(pickle);
    }

}
