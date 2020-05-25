package io.cucumber.core.filter;

import io.cucumber.core.gherkin.Pickle;
import io.cucumber.tagexpressions.Expression;
import io.cucumber.tagexpressions.TagExpressionException;
import io.cucumber.tagexpressions.TagExpressionParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

final class TagPredicate implements Predicate<Pickle> {

    private final List<Expression> expressions;

    TagPredicate(String tagExpression) {
        this(tagExpression.isEmpty() ? emptyList() : singletonList(tagExpression));
    }

    TagPredicate(List<String> tagExpressions) {
        expressions = Optional.ofNullable(tagExpressions)
                .map(this::tryParseTagExpression)
                .orElse(new ArrayList<>());
    }

    private List<Expression> tryParseTagExpression(List<String> tagExpressions) {
        try {
            return tagExpressions.stream()
                    .map(TagExpressionParser::parse)
                    .collect(Collectors.toList());
        } catch (TagExpressionException tee) {
            throw new RuntimeException(tee.getMessage() + String.format(" at '%s'", this.getClass().getName()), tee);
        }
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
