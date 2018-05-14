package io.cucumber.stepexpression;

import cucumber.runtime.CucumberException;
import io.cucumber.cucumberexpressions.UndefinedParameterTypeException;
import io.cucumber.datatable.DataTableTypeRegistryTableConverter;
import io.cucumber.datatable.DataTable;

import io.cucumber.cucumberexpressions.Expression;

import java.lang.reflect.Type;
import java.util.List;

import static java.util.Collections.singletonList;

public final class StepExpressionFactory {

    private final io.cucumber.cucumberexpressions.ExpressionFactory expressionFactory;
    private final DataTableTypeRegistryTableConverter tableConverter;

    private static final DocStringTransformer<String> DOC_STRING_IDENTITY = new DocStringTransformer<String>() {
        @Override
        public String transform(String input) {
            return input;
        }
    };

    public StepExpressionFactory(TypeRegistry registry) {
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

        final Expression expression;
        try {
            expression = expressionFactory.createExpression(expressionString);
        } catch (UndefinedParameterTypeException e) {
            throw registerTypeInConfiguration(expressionString, e);
        }

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

    private CucumberException registerTypeInConfiguration(String expressionString, UndefinedParameterTypeException e) {
        return new CucumberException(String.format("" +
                "Could not create a cucumber expression for '%s'.\n" +
                "It appears you did not register parameter type. The details are in the stacktrace below.\n" +
                "You can find the documentation here: https://docs.cucumber.io/cucumber/cucumber-expressions/",
            expressionString
        ), e);
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
