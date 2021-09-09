package io.cucumber.core.stepexpression;

import io.cucumber.cucumberexpressions.Group;

import java.lang.reflect.Type;

public final class ExpressionArgument implements Argument {

    private final io.cucumber.cucumberexpressions.Argument<?> argument;

    ExpressionArgument(io.cucumber.cucumberexpressions.Argument<?> argument) {
        this.argument = argument;
    }

    @Override
    public Object getValue() {
        return argument.getValue();
    }

    public Group getGroup() {
        return argument.getGroup();
    }

    public Type getType() {
        return argument.getType();
    }

    public String getParameterTypeName() {
        return argument.getParameterType().getName();
    }

    @Override
    public String toString() {
        return argument.getGroup() == null ? null : argument.getGroup().getValue();
    }

}
