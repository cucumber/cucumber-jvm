package io.cucumber.core.stepexpression;

import io.cucumber.core.gherkin.Step;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.List;

public final class ArgumentMatcher {

    private final StepExpression expression;

    public ArgumentMatcher(StepExpression expression) {
        this.expression = expression;
    }

    public @Nullable List<Argument> argumentsFrom(Step step, Type... types) {
        return expression.match(step, types);
    }

}
