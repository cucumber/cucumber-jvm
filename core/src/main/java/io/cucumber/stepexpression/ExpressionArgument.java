package io.cucumber.stepexpression;

import io.cucumber.cucumberexpressions.Group;

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

    @Override
    public String toString() {
        return argument.getGroup() == null ? null : argument.getGroup().getValue();
    }
}
