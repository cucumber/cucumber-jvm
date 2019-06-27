package io.cucumber.core.runner;

import io.cucumber.core.event.Argument;
import io.cucumber.core.stepexpression.ExpressionArgument;

import java.util.ArrayList;
import java.util.List;

final class DefinitionArgument implements Argument {

    private final io.cucumber.cucumberexpressions.Group group;

    private DefinitionArgument(ExpressionArgument expressionArgument) {
        group = expressionArgument.getGroup();
    }

    static List<Argument> createArguments(List<io.cucumber.core.stepexpression.Argument> match) {
        List<Argument> args = new ArrayList<Argument>();
        for (io.cucumber.core.stepexpression.Argument argument : match) {
            if (argument instanceof ExpressionArgument) {
                args.add(new DefinitionArgument((ExpressionArgument) argument));
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
