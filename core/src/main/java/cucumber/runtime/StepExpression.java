package cucumber.runtime;

import cucumber.runtime.datatable.DataTableArgument;
import cucumber.runtime.datatable.DocStringArgument;
import io.cucumber.cucumberexpressions.Argument;
import io.cucumber.cucumberexpressions.Expression;
import cucumber.api.datatable.DocStringTransformer;
import cucumber.api.datatable.RawTableTransformer;

import java.util.List;
import java.util.regex.Pattern;

public final class StepExpression implements  Expression {

    private final Expression expression;
    private final DocStringTransformer<?> docStringType;
    private final RawTableTransformer<?> tableType;

    StepExpression(Expression expression, DocStringTransformer<?> docStringType, RawTableTransformer<?> tableType) {
        this.expression = expression;
        this.docStringType = docStringType;
        this.tableType = tableType;
    }

    @Override
    public List<Argument<?>> match(String text) {
        return expression.match(text);
    }

    @Override
    public Pattern getRegexp() {
        return expression.getRegexp();
    }

    @Override
    public String getSource() {
        return expression.getSource();
    }

    public List<Argument<?>> match(String text, List<List<String>> tableArgument) {
        List<Argument<?>> list = expression.match(text);

        if (list == null) {
            return null;
        }

        //noinspection RawTableTransformer<?> fits in Argument<?>
        list.add(new DataTableArgument(tableType, tableArgument));

        return list;

    }

    public List<Argument<?>> match(String text, String docStringArgument) {
        List<Argument<?>> list = expression.match(text);
        if (list == null) {
            return null;
        }

        //noinspection DocStringTransformer<?> fits in Argument<?>
        list.add(new DocStringArgument(docStringType, docStringArgument));

        return list;
    }

}
