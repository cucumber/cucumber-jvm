package io.cucumber.core.filter;

import io.cucumber.core.gherkin.Pickle;
import io.cucumber.tagexpressions.Expression;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static java.util.Collections.emptyList;

final class TagPredicate implements Predicate<Pickle> {

    private final List<Expression> expressions;

    TagPredicate(Expression tagExpression) {
        this(Optional.ofNullable(tagExpression)
                .map(Collections::singletonList)
                .orElse(emptyList()));
    }

    TagPredicate(List<Expression> exprs) {
        expressions = Optional.ofNullable(exprs)
                .orElse(emptyList());

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
