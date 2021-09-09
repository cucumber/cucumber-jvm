package io.cucumber.core.runner;

import io.cucumber.core.stepexpression.ExpressionArgument;
import io.cucumber.plugin.event.Argument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

final class DefinitionArgument implements Argument {

    private final ExpressionArgument argument;
    private final io.cucumber.cucumberexpressions.Group group;

    private DefinitionArgument(ExpressionArgument argument) {
        this.argument = argument;
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
    public String getParameterTypeName() {
        return argument.getParameterTypeName();
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

    @Override
    public io.cucumber.plugin.event.Group getGroup() {
        return group == null ? null : new Group(group);
    }

    private static final class Group implements io.cucumber.plugin.event.Group {

        private final io.cucumber.cucumberexpressions.Group group;
        private final List<io.cucumber.plugin.event.Group> children;

        private Group(io.cucumber.cucumberexpressions.Group group) {
            this.group = group;
            children = group.getChildren().stream()
                    .map(Group::new)
                    .collect(Collectors.toList());
        }

        @Override
        public Collection<io.cucumber.plugin.event.Group> getChildren() {
            return children;
        }

        @Override
        public String getValue() {
            return group.getValue();
        }

        @Override
        public int getStart() {
            return group.getStart();
        }

        @Override
        public int getEnd() {
            return group.getEnd();
        }

    }

}
