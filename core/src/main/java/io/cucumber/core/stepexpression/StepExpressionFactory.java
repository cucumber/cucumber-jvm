package io.cucumber.core.stepexpression;

import io.cucumber.core.backend.ParameterInfo;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.cucumberexpressions.Expression;
import io.cucumber.cucumberexpressions.ExpressionFactory;
import io.cucumber.cucumberexpressions.UndefinedParameterTypeException;
import io.cucumber.datatable.DataTable;
import io.cucumber.datatable.DataTableTypeRegistryTableConverter;
import io.cucumber.docstring.DocString;
import io.cucumber.docstring.DocStringTypeRegistryDocStringConverter;
import io.cucumber.messages.Messages.Envelope;
import io.cucumber.messages.Messages.UndefinedParameterType;

import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public final class StepExpressionFactory {

    private final ExpressionFactory expressionFactory;
    private final DataTableTypeRegistryTableConverter tableConverter;
    private final DocStringTypeRegistryDocStringConverter docStringConverter;
    private final EventBus bus;

    public StepExpressionFactory(StepTypeRegistry registry, EventBus bus) {
        this.expressionFactory = new ExpressionFactory(registry.parameterTypeRegistry());
        this.tableConverter = new DataTableTypeRegistryTableConverter(registry.dataTableTypeRegistry());
        this.docStringConverter = new DocStringTypeRegistryDocStringConverter(registry.docStringTypeRegistry());
        this.bus = bus;
    }

    public StepExpression createExpression(StepDefinition stepDefinition) {
        String expression = stepDefinition.getPattern();
        List<ParameterInfo> parameterInfos = stepDefinition.parameterInfos();
        if (parameterInfos == null || parameterInfos.isEmpty()) {
            Supplier<Type> typeResolver = () -> {
                throw stepDefinitionDoesNotTakeAnyParameter(stepDefinition);
            };
            return createExpression(expression, typeResolver, false);
        } else {
            ParameterInfo parameterInfo = parameterInfos.get(parameterInfos.size() - 1);
            Supplier<Type> typeResolver = parameterInfo.getTypeResolver()::resolve;
            boolean transposed = parameterInfo.isTransposed();
            return createExpression(expression, typeResolver, transposed);
        }
    }

    private StepExpression createExpression(String expressionString, Supplier<Type> tableOrDocStringType, boolean transpose) {
        requireNonNull(expressionString, "expressionString can not be null");
        requireNonNull(tableOrDocStringType, "tableOrDocStringType can not be null");

        final Expression expression = crateExpression(expressionString);

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

    private Expression crateExpression(String expressionString) {
        final Expression expression;
        try {
            expression = expressionFactory.createExpression(expressionString);
        } catch (UndefinedParameterTypeException e) {
            bus.send(Envelope.newBuilder()
                .setUndefinedParameterType(UndefinedParameterType.newBuilder()
                    .setExpression(expressionString)
                    .setName(e.getUndefinedParameterTypeName()))
                .build()
            );
            throw registerTypeInConfiguration(expressionString, e);
        }
        return expression;
    }

    private static CucumberException stepDefinitionDoesNotTakeAnyParameter(StepDefinition stepDefinition) {
        return new CucumberException(format(
            "step definition at %s does not take any parameters",
            stepDefinition.getLocation()
        ));
    }

    private CucumberException registerTypeInConfiguration(String expressionString, UndefinedParameterTypeException e) {
        return new CucumberException(format("" +
                "Could not create a cucumber expression for '%s'.\n" +
                "It appears you did not register parameter type. The details are in the stacktrace below.\n" +
                "You can find the documentation here: https://docs.cucumber.io/cucumber/cucumber-expressions/",
            expressionString
        ), e);
    }

}
