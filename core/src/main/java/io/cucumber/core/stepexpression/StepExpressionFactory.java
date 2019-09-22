package io.cucumber.core.stepexpression;

import io.cucumber.core.exception.CucumberException;
import io.cucumber.cucumberexpressions.Expression;
import io.cucumber.cucumberexpressions.UndefinedParameterTypeException;
import io.cucumber.datatable.DataTable;
import io.cucumber.datatable.DataTableTypeRegistryTableConverter;
import io.cucumber.docstring.DocString;
import io.cucumber.docstring.DocStringTypeRegistryDocStringConverter;

import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Supplier;

public final class StepExpressionFactory {

    private final io.cucumber.cucumberexpressions.ExpressionFactory expressionFactory;
    private final DataTableTypeRegistryTableConverter tableConverter;
    private final DocStringTypeRegistryDocStringConverter docStringConverter;

    public StepExpressionFactory(TypeRegistry registry) {
        this.expressionFactory = new io.cucumber.cucumberexpressions.ExpressionFactory(registry.parameterTypeRegistry());
        this.tableConverter = new DataTableTypeRegistryTableConverter(registry.dataTableTypeRegistry());
        this.docStringConverter = new DocStringTypeRegistryDocStringConverter(registry.docStringTypeRegistry());
    }

    public StepExpression createExpression(String expressionString) {
        if (expressionString == null) throw new NullPointerException("expression can not be null");
        Expression expression = expressionFactory.createExpression(expressionString);

        RawTableTransformer<DataTable> toDataTable = raw -> DataTable.create(raw, tableConverter);
        DocStringTransformer<Object> toDocString = (String input, String contentType) -> input;
        return new StepExpression(expression, toDocString, toDataTable);
    }

    public StepExpression createExpression(String expressionString, Type tableOrDocStringType) {
        return createExpression(expressionString, () -> tableOrDocStringType, false);
    }

    public StepExpression createExpression(String expressionString, Supplier<Type> tableOrDocStringType) {
        return createExpression(expressionString, tableOrDocStringType, false);
    }

    public StepExpression createExpression(String expressionString, Type tableOrDocStringType, boolean transpose) {
        return createExpression(expressionString, () -> tableOrDocStringType, transpose);
    }

    public StepExpression createExpression(String expressionString, Supplier<Type> tableOrDocStringType, boolean transpose) {
        if (expressionString == null) throw new NullPointerException("expressionString can not be null");
        if (tableOrDocStringType == null) throw new NullPointerException("tableOrDocStringType can not be null");

        final Expression expression;
        try {
            expression = expressionFactory.createExpression(expressionString);
        } catch (UndefinedParameterTypeException e) {
            throw registerTypeInConfiguration(expressionString, e);
        }

        RawTableTransformer<?> tableTransform = (List<List<String>> raw) -> {
            DataTable dataTable = DataTable.create(raw, StepExpressionFactory.this.tableConverter);
            Type targetType = tableOrDocStringType.get();
            return dataTable.convert(Object.class.equals(targetType) ? DataTable.class : targetType, transpose);
        };

        DocStringTransformer<?> docStringTransform = (text, contentType) -> {
            DocString docString = DocString.create(text, contentType, docStringConverter);
            Type targetType = tableOrDocStringType.get();
            return docString.convert(Object.class.equals(targetType) ? DocString.class : targetType);
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
}
