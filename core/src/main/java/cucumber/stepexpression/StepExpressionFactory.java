package cucumber.stepexpression;

import cucumber.api.TypeRegistry;
import cucumber.api.datatable.DataTableTypeRegistryTableConverter;
import cucumber.api.datatable.DataTableType;
import cucumber.api.datatable.DataTable;

import io.cucumber.cucumberexpressions.CucumberExpressionException;
import io.cucumber.cucumberexpressions.Expression;
import io.cucumber.cucumberexpressions.ParameterType;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public final class StepExpressionFactory {

    private final io.cucumber.cucumberexpressions.ExpressionFactory expressionFactory;
    private final DataTableTypeRegistryTableConverter tableConverter;
    private final TypeRegistry registry;

    private static final DocStringTransformer<String> DOC_STRING_IDENTITY = new DocStringTransformer<String>() {
        @Override
        public String transform(String input) {
            return input;
        }
    };

    private static final RawTableTransformer<DataTable> TO_DATA_TABLE = new RawTableTransformer<DataTable>() {
        @Override
        public DataTable transform(List<List<String>> raw) {
            return DataTable.create(raw);
        }
    };

    public StepExpressionFactory(TypeRegistry registry) {
        this.registry = registry;
        this.expressionFactory = new io.cucumber.cucumberexpressions.ExpressionFactory(registry.parameterTypeRegistry());
        this.tableConverter = new DataTableTypeRegistryTableConverter(registry.dataTableTypeRegistry());
    }

    public StepExpression createExpression(String expressionString) {
        if (expressionString == null) throw new CucumberExpressionException("expression can not be null");
        Expression expression = expressionFactory.createExpression(expressionString);
        return new StepExpression(expression, DOC_STRING_IDENTITY, TO_DATA_TABLE);
    }

    public StepExpression createExpression(String expressionString, Type tableOrDocStringType) {
        return createExpression(expressionString, tableOrDocStringType, false);
    }

    public StepExpression createExpression(String expressionString, final Type tableOrDocStringType, final boolean transpose) {
        if (expressionString == null) throw new CucumberExpressionException("expressionString can not be null");
        if (tableOrDocStringType == null) throw new CucumberExpressionException("tableOrDocStringType can not be null");

        Expression expression = expressionFactory.createExpression(expressionString);

        RawTableTransformer<?> tableTransform = new RawTableTransformer<Object>() {
            @Override
            public Object transform(List<List<String>> raw) {
                return DataTable.create(raw, StepExpressionFactory.this.tableConverter).convert(tableOrDocStringType, transpose);
            }
        };

        DocStringTransformer<?> docStringTransform = new DocStringTransformer<Object>() {
            @Override
            public Object transform(String raw) {
                //TODO: Fetch a transform from a registry
                return raw;
            }
        };
        return new StepExpression(expression, docStringTransform, tableTransform);
    }

    public StepExpression createExpression(String expressionString, final String tableOrDocStringType) {
        if (expressionString == null) throw new CucumberExpressionException("expressionString can not be null");
        if (tableOrDocStringType == null) throw new CucumberExpressionException("tableOrDocStringType can not be null");

        Expression expression = expressionFactory.createExpression(expressionString);

        RawTableTransformer<?> tableTransform = new RawTableTransformer<Object>() {
            @Override
            public Object transform(List<List<String>> raw) {
                DataTableType type = registry.lookupTableTypeByName(tableOrDocStringType);
                if (type == null) {
                    throw new UndefinedTableTypeException(tableOrDocStringType);
                }
                return type.transform(raw);
            }
        };

        DocStringTransformer<?> docStringTransform = new DocStringTransformer<Object>() {
            @Override
            public Object transform(String input) {
                final ParameterType<?> type = registry.lookupParameterTypeByTypeName(tableOrDocStringType);
                if (type == null) {
                    throw new UndefinedTableTypeException(tableOrDocStringType);
                }

                return type.transform(Collections.singletonList(input));
            }
        };

        return new StepExpression(expression, docStringTransform, tableTransform);
    }

}
