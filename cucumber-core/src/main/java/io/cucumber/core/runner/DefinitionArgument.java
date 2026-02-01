package io.cucumber.core.runner;

import io.cucumber.core.stepexpression.ExpressionArgument;
import io.cucumber.plugin.event.Argument;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
            if (argument instanceof ExpressionArgument expressionArgument) {
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
    public @Nullable String getValue() {
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

    @Override
    public io.cucumber.plugin.event.Group getGroup() {
        return new Group(group);
    }

    private static final class Group implements io.cucumber.plugin.event.Group {

        private final io.cucumber.cucumberexpressions.Group group;
        private final List<io.cucumber.plugin.event.Group> children;

        private Group(io.cucumber.cucumberexpressions.Group group) {
            this.group = group;
            children = group.getChildren()
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(Group::new)
                    .collect(Collectors.toList());
        }

        @Override
        public Collection<io.cucumber.plugin.event.Group> getChildren() {
            return children;
        }

        @Override
        public @Nullable String getValue() {
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
