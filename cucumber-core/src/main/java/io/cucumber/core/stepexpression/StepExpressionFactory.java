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
import io.cucumber.messages.types.Envelope;
import io.cucumber.messages.types.UndefinedParameterType;

import java.util.List;
import java.util.function.Function;

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
        List<ParameterInfo> parameterInfos = stepDefinition.parameterInfos();
        String expressionString = stepDefinition.getPattern();
        var expression = crateExpression(expressionString);

        Function<Integer, ParameterInfo> safeGetParameterInfos = argumentIndex -> {
            if (argumentIndex < parameterInfos.size()) {
                return parameterInfos.get(argumentIndex);
            }
            throw new CucumberException("step definition at %s does not take enough parameters. Expected %s parameters."
                    .formatted(stepDefinition.getLocation(), argumentIndex + 1));
        };

        RawTableTransformer<?> tableTransform = (argumentIndex, raw) -> {
            var parameterInfo = safeGetParameterInfos.apply(argumentIndex);
            var targetType = parameterInfo.getType();
            var transpose = parameterInfo.isTransposed();
            var dataTable = DataTable.create(raw, this.tableConverter);
            return dataTable.convert(Object.class.equals(targetType) ? DataTable.class : targetType, transpose);
        };

        DocStringTransformer<?> docStringTransform = (argumentIndex, text, contentType) -> {
            var parameterInfo = safeGetParameterInfos.apply(argumentIndex);
            var targetType = parameterInfo.getType();
            var docString = DocString.create(text, contentType, docStringConverter);
            return docString.convert(Object.class.equals(targetType) ? DocString.class : targetType);
        };
        return new StepExpression(expression, docStringTransform, tableTransform);
    }

    private Expression crateExpression(String expressionString) {
        final Expression expression;
        try {
            expression = expressionFactory.createExpression(expressionString);
        } catch (UndefinedParameterTypeException e) {
            bus.send(Envelope.of(new UndefinedParameterType(
                expressionString,
                e.getUndefinedParameterTypeName())));
            throw registerTypeInConfiguration(expressionString, e);
        }
        return expression;
    }

    private CucumberException registerTypeInConfiguration(String expressionString, UndefinedParameterTypeException e) {
        return new CucumberException("""
                Could not create a cucumber expression for '%s'.
                It appears you did not register a parameter type.""".formatted(expressionString), e);
    }

}
