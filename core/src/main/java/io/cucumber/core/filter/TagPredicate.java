package io.cucumber.core.filter;

import io.cucumber.core.gherkin.Pickle;
import io.cucumber.tagexpressions.Expression;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

final class TagPredicate implements Predicate<Pickle> {

    private final List<Expression> expressions;

    TagPredicate(List<Expression> tagExpressions) {
        expressions = Objects.requireNonNull(tagExpressions);
    }

    @Override
    public boolean test(Pickle pickle) {
        if (expressions.isEmpty()) {
            return true;
        }

        List<String> tags = pickle.getTags();
        return expressions.stream()
                .allMatch(expression -> expression.evaluate(tags));
    }

}
