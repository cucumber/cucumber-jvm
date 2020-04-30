package io.cucumber.core.runner;

import io.cucumber.core.backend.ParameterInfo;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.gherkin.Step;
import io.cucumber.core.stepexpression.Argument;
import io.cucumber.core.stepexpression.ArgumentMatcher;
import io.cucumber.core.stepexpression.StepExpression;
import io.cucumber.core.stepexpression.StepExpressionFactory;
import io.cucumber.core.stepexpression.StepTypeRegistry;
import io.cucumber.cucumberexpressions.Expression;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

final class CoreStepDefinition {

    private final UUID id;
    private final StepExpression expression;
    private final ArgumentMatcher argumentMatcher;
    private final StepDefinition stepDefinition;
    private final Type[] types;

    CoreStepDefinition(UUID id, StepDefinition stepDefinition, StepExpression expression) {
        this.id = requireNonNull(id);
        this.stepDefinition = requireNonNull(stepDefinition);
        this.expression = expression;
        this.argumentMatcher = new ArgumentMatcher(this.expression);
        this.types = getTypes(stepDefinition.parameterInfos());
    }

    StepExpression getExpression() {
        return expression;
    }

    StepDefinition getStepDefinition() {
        return stepDefinition;
    }

    List<Argument> matchedArguments(Step step) {
        return argumentMatcher.argumentsFrom(step, types);
    }

    UUID getId() {
        return id;
    }

    private static Type[] getTypes(List<ParameterInfo> parameterInfos) {
        if (parameterInfos == null) {
            return new Type[0];
        }

        Type[] types = new Type[parameterInfos.size()];
        for (int i = 0; i < types.length; i++) {
            types[i] = parameterInfos.get(i).getType();
        }
        return types;
    }
}
