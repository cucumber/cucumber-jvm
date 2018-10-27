package io.cucumber.stepexpression;

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

    public List<Argument> match(String text, Type... types) {
        List<io.cucumber.cucumberexpressions.Argument<?>> match = expression.match(text, types);
        if (match == null) {
            return null;
        }
        return wrapPlusOne(match);
    }

    public String getSource() {
        return expression.getSource();
    }

    public List<Argument> match(String text, List<List<String>> tableArgument, Type... types) {
        List<Argument> list = match(text, types);

        if (list == null) {
            return null;
        }

        list.add(new DataTableArgument(tableType, tableArgument));

        return list;

    }

    public List<Argument> match(String text, String docStringArgument, Type... types) {
        List<Argument> list = match(text, types);
        if (list == null) {
            return null;
        }

        list.add(new DocStringArgument(docStringType, docStringArgument));

        return list;
    }


    private static List<Argument> wrapPlusOne(List<io.cucumber.cucumberexpressions.Argument<?>> match) {
        List<Argument> copy = new ArrayList<Argument>(match.size() + 1);
        for (io.cucumber.cucumberexpressions.Argument<?> argument : match) {
            copy.add(new ExpressionArgument(argument));
        }
        return copy;
    }

}
