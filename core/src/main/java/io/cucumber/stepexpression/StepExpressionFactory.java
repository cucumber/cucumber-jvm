package io.cucumber.stepexpression;

import cucumber.api.TypeRegistry;
import io.cucumber.datatable.DataTableTypeRegistryTableConverter;
import io.cucumber.datatable.DataTableType;
import io.cucumber.datatable.DataTable;

import io.cucumber.cucumberexpressions.Expression;

import java.lang.reflect.Type;
import java.util.List;

import static java.util.Collections.singletonList;

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

    public StepExpressionFactory(TypeRegistry registry) {
        this.registry = registry;
        this.expressionFactory = new io.cucumber.cucumberexpressions.ExpressionFactory(registry.parameterTypeRegistry());
        this.tableConverter = new DataTableTypeRegistryTableConverter(registry.dataTableTypeRegistry());
    }

    public StepExpression createExpression(String expressionString) {
        if (expressionString == null) throw new NullPointerException("expression can not be null");
        Expression expression = expressionFactory.createExpression(expressionString);

        RawTableTransformer<DataTable> toDataTable = new RawTableTransformer<DataTable>() {
            @Override
            public DataTable transform(List<List<String>> raw) {
                return DataTable.create(raw, tableConverter);
            }
        };
        return new StepExpression(expression, DOC_STRING_IDENTITY, toDataTable);
    }

    public StepExpression createExpression(String expressionString, Type tableOrDocStringType) {
        return createExpression(expressionString, new ResolvedType(tableOrDocStringType), false);
    }

    public StepExpression createExpression(String expressionString, TypeResolver tableOrDocStringType) {
        return createExpression(expressionString, tableOrDocStringType, false);
    }
    public StepExpression createExpression(String expressionString, final Type tableOrDocStringType, final boolean transpose) {
        return createExpression(expressionString, new ResolvedType(tableOrDocStringType), transpose);
    }

    public StepExpression createExpression(String expressionString, final TypeResolver tableOrDocStringType, final boolean transpose) {
        if (expressionString == null) throw new NullPointerException("expressionString can not be null");
        if (tableOrDocStringType == null) throw new NullPointerException("tableOrDocStringType can not be null");

        Expression expression = expressionFactory.createExpression(expressionString);

        RawTableTransformer<?> tableTransform = new RawTableTransformer<Object>() {
            @Override
            public Object transform(List<List<String>> raw) {
                return DataTable.create(raw, StepExpressionFactory.this.tableConverter)
                    .convert(tableOrDocStringType.resolve(), transpose);
            }
        };

        DocStringTransformer<?> docStringTransform = new DocStringTransformer<Object>() {
            @Override
            public Object transform(String docString) {
                return DataTable.create(singletonList(singletonList(docString)), StepExpressionFactory.this.tableConverter)
                    .convert(tableOrDocStringType.resolve(), transpose);
            }
        };
        return new StepExpression(expression, docStringTransform, tableTransform);
    }


    public StepExpression createExpression(String expressionString, String tableOrDocStringType) {
        return createExpression(expressionString, tableOrDocStringType, false);
    }

    public StepExpression createExpression(String expressionString, final String tableOrDocStringType, final boolean transpose) {
        if (expressionString == null) throw new NullPointerException("expressionString can not be null");
        if (tableOrDocStringType == null) throw new NullPointerException("tableOrDocStringType can not be null");

        Expression expression = expressionFactory.createExpression(expressionString);

        RawTableTransformer<?> tableTransform = new RawTableTransformer<Object>() {
            @Override
            public Object transform(List<List<String>> raw) {
                DataTableType type = registry.lookupTableTypeByName(tableOrDocStringType);
                if (type == null) {
                    throw new UndefinedTableTypeException(tableOrDocStringType);
                }
                if (transpose) {
                    raw = DataTable.create(raw, StepExpressionFactory.this.tableConverter).transpose().cells();
                }
                return type.transform(raw);
            }
        };

        DocStringTransformer<?> docStringTransform = new DocStringTransformer<Object>() {
            @Override
            public Object transform(String docString) {
                DataTableType type = registry.lookupTableTypeByName(tableOrDocStringType);
                if (type == null) {
                    throw new UndefinedTableTypeException(tableOrDocStringType);
                }
                return type.transform(singletonList(singletonList(docString)));
            }
        };

        return new StepExpression(expression, docStringTransform, tableTransform);
    }

    private static final class ResolvedType implements TypeResolver {

        private final Type type;

        private ResolvedType(Type type) {
            this.type = type;
        }

        @Override
        public Type resolve() {
            return type;
        }
    }

}
