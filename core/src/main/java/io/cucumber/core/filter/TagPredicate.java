package io.cucumber.core.filter;

import io.cucumber.core.gherkin.Pickle;
import io.cucumber.tagexpressions.Expression;
import io.cucumber.tagexpressions.TagExpressionParser;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;


final class TagPredicate implements Predicate<Pickle> {
    private final List<Expression> expressions = new ArrayList<>();

    TagPredicate(String tagExpression) {
        this(tagExpression.isEmpty() ? emptyList() : singletonList(tagExpression));
    }

    TagPredicate(List<String> tagExpressions) {
        if (tagExpressions == null) {
            return;
        }
        TagExpressionParser parser = new TagExpressionParser();
        for (String tagExpression : tagExpressions) {
            expressions.add(parser.parse(tagExpression));
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
