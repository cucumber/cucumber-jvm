package io.cucumber.core.runner;

import gherkin.pickles.PickleStep;
import io.cucumber.core.backend.ParameterInfo;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.stepexpression.Argument;
import io.cucumber.core.stepexpression.ArgumentMatcher;
import io.cucumber.core.stepexpression.StepExpression;
import io.cucumber.core.stepexpression.StepExpressionFactory;
import io.cucumber.core.stepexpression.TypeRegistry;
import io.cucumber.core.stepexpression.TypeResolver;

import java.lang.reflect.Type;
import java.util.List;

import static java.util.Objects.requireNonNull;

class CoreStepDefinition {

    private final StepExpression expression;
    private final ArgumentMatcher argumentMatcher;
    private final StepDefinition stepDefinition;
    private final Type[] types;

    CoreStepDefinition(StepDefinition stepDefinition, TypeRegistry typeRegistry) {
        this.stepDefinition = requireNonNull(stepDefinition);
        List<ParameterInfo> parameterInfos = stepDefinition.parameterInfos();
        this.expression = createExpression(parameterInfos, stepDefinition.getPattern(), typeRegistry);
        this.argumentMatcher = new ArgumentMatcher(this.expression);
        this.types = getTypes(parameterInfos);
    }

    private StepExpression createExpression(List<ParameterInfo> parameterInfos, String expression, TypeRegistry typeRegistry) {
        if (parameterInfos == null || parameterInfos.isEmpty()) {
            return new StepExpressionFactory(typeRegistry).createExpression(expression);
        } else {
            ParameterInfo parameterInfo = parameterInfos.get(parameterInfos.size() - 1);
            TypeResolver typeResolver = parameterInfo.getTypeResolver()::resolve;
            boolean transposed = parameterInfo.isTransposed();
            return new StepExpressionFactory(typeRegistry).createExpression(expression, typeResolver, transposed);
        }
    }

    public String getPattern() {
        return expression.getSource();
    }

    public StepDefinition getStepDefinition() {
        return stepDefinition;
    }

    List<Argument> matchedArguments(PickleStep step) {
        return argumentMatcher.argumentsFrom(step, types);
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
