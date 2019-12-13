package io.cucumber.core.runner;

import io.cucumber.core.stepexpression.ExpressionArgument;
import io.cucumber.plugin.event.Argument;

import java.util.ArrayList;
import java.util.List;

final class DefinitionArgument implements Argument {

    private final io.cucumber.cucumberexpressions.Group group;

    private DefinitionArgument(ExpressionArgument argument) {
        this.group = argument.getGroup();
    }

    static List<Argument> createArguments(List<io.cucumber.core.stepexpression.Argument> match) {
        List<Argument> args = new ArrayList<>();
        for (io.cucumber.core.stepexpression.Argument argument : match) {
            if (argument instanceof ExpressionArgument) {
                ExpressionArgument expressionArgument = (ExpressionArgument) argument;
                args.add(new DefinitionArgument(expressionArgument));
            }
        }
        return args;
    }

    @Override
    public String getValue() {
        return group == null ? null : group.getValue();
    }

    @Override
    public int getStart() {
        return group == null ? -1 : group.getStart();
    }

    @Override
    public int getEnd() {
        return group == null ? -1 : group.getEnd();
    }
}
