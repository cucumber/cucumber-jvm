package io.cucumber.core.stepexpression;

import io.cucumber.cucumberexpressions.Expression;
import io.cucumber.cucumberexpressions.Group;
import io.cucumber.messages.Messages.StepMatchArgument;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class StepExpression {

    private final Expression expression;
    private final DocStringTransformer<?> docStringType;
    private final RawTableTransformer<?> tableType;

    StepExpression(Expression expression, DocStringTransformer<?> docStringType, RawTableTransformer<?> tableType) {
        this.expression = expression;
        this.docStringType = docStringType;
        this.tableType = tableType;
    }

    public List<Argument> match(String text, Type... types) {
        List<io.cucumber.cucumberexpressions.Argument<?>> match = expression.match(text, types);
        if (match == null) {
            return null;
        }
        return wrapPlusOne(match);
    }

    public Iterable<StepMatchArgument> getStepMatchArguments(String text, Type[] types) {
        List<io.cucumber.cucumberexpressions.Argument<?>> arguments = expression.match(text, types);
        if (arguments == null) {
            return null;
        }
        return arguments.stream().map(arg -> StepMatchArgument.newBuilder()
            .setParameterTypeName(arg.getParameterType().getName())
            .setGroup(makeMessageGroup(arg.getGroup()))
            .build()
        ).collect(Collectors.toList());
    }

    private static StepMatchArgument.Group makeMessageGroup(Group group) {
        StepMatchArgument.Group.Builder builder = StepMatchArgument.Group.newBuilder();
        if (group.getValue() != null) {
            builder.setValue(group.getValue());
        }
        return builder
            .addAllChildren(group.getChildren().stream().map(StepExpression::makeMessageGroup).collect(Collectors.toList()))
            .build();
    }

    public String getSource() {
        return expression.getSource();
    }

    public List<Argument> match(String text, List<List<String>> cells, Type... types) {
        List<Argument> list = match(text, types);

        if (list == null) {
            return null;
        }

        list.add(new DataTableArgument(tableType, cells));

        return list;

    }

    public List<Argument> match(String text, String content, String contentType, Type... types) {
        List<Argument> list = match(text, types);
        if (list == null) {
            return null;
        }

        list.add(new DocStringArgument(this.docStringType, content, contentType));

        return list;
    }

    private static List<Argument> wrapPlusOne(List<io.cucumber.cucumberexpressions.Argument<?>> match) {
        List<Argument> copy = new ArrayList<>(match.size() + 1);
        for (io.cucumber.cucumberexpressions.Argument<?> argument : match) {
            copy.add(new ExpressionArgument(argument));
        }
        return copy;
    }
}
