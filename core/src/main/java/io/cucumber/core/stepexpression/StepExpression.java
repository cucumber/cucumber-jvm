package io.cucumber.core.stepexpression;

import io.cucumber.cucumberexpressions.Expression;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public final class StepExpression {

    private final Expression expression;
    private final DocStringTransformer<?> docStringType;
    private final RawTableTransformer<?> tableType;

    StepExpression(Expression expression, DocStringTransformer<?> docStringType, RawTableTransformer<?> tableType) {
        this.expression = expression;
        this.docStringType = docStringType;
        this.tableType = tableType;
    }

    public Class<? extends Expression> getExpressionType() {
        return expression.getClass();
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

    public List<Argument> match(String text, Type... types) {
        List<io.cucumber.cucumberexpressions.Argument<?>> match = expression.match(text, types);
        if (match == null) {
            return null;
        }
        return wrapPlusOne(match);
    }

    private static List<Argument> wrapPlusOne(List<io.cucumber.cucumberexpressions.Argument<?>> match) {
        List<Argument> copy = new ArrayList<>(match.size() + 1);
        for (io.cucumber.cucumberexpressions.Argument<?> argument : match) {
            copy.add(new ExpressionArgument(argument));
        }
        return copy;
    }

    public List<Argument> match(String text, String content, String contentType, Type... types) {
        List<Argument> list = match(text, types);
        if (list == null) {
            return null;
        }

        list.add(new DocStringArgument(this.docStringType, content, contentType));

        return list;
    }

}
