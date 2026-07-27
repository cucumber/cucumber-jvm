package io.cucumber.core.stepexpression;

import io.cucumber.core.gherkin.Step;
import io.cucumber.cucumberexpressions.Expression;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public final class StepExpression {

    private final Expression expression;
    private final DocStringTransformer<?> docStringTransformer;
    private final RawTableTransformer<?> dataTableTransformer;

    StepExpression(
            Expression expression, DocStringTransformer<?> docStringTransformer,
            RawTableTransformer<?> dataTableTransformer
    ) {
        this.expression = requireNonNull(expression);
        this.docStringTransformer = requireNonNull(docStringTransformer);
        this.dataTableTransformer = requireNonNull(dataTableTransformer);
    }

    public Class<? extends Expression> getExpressionType() {
        return expression.getClass();
    }

    public String getSource() {
        return expression.getSource();
    }

    @Nullable
    List<Argument> match(Step step, Type... types) {
        return expression.match(step.getText(), types)
                .map(expressionArguments -> {
                    var stepArguments = step.getArguments();
                    int stepArgumentSize = stepArguments.size();
                    var arguments = new ArrayList<Argument>(expressionArguments.size() + stepArgumentSize);
                    expressionArguments.stream()
                            .map(ExpressionArgument::new)
                            .forEach(arguments::add);

                    for (int i = 0; i < stepArgumentSize; i++) {
                        io.cucumber.core.gherkin.Argument stepArgument = stepArguments.get(i);
                        Argument argument = createArgument(i, stepArgument);
                        arguments.add(argument);
                    }
                    return arguments;
                })
                .orElse(null);
    }

    private Argument createArgument(int argumentIndex, io.cucumber.core.gherkin.@Nullable Argument argument) {
        if (argument instanceof io.cucumber.core.gherkin.DocStringArgument docString) {
            var content = docString.getContent();
            var contentType = docString.getMediaType();
            return new DocStringArgument(argumentIndex, this.docStringTransformer, content, contentType);
        }
        if (argument instanceof io.cucumber.core.gherkin.DataTableArgument table) {
            var cells = emptyCellsToNull(table.cells());
            return new DataTableArgument(argumentIndex, dataTableTransformer, cells);
        }
        throw new IllegalStateException("Argument was neither PickleString nor PickleTable");
    }

    private static List<List<@Nullable String>> emptyCellsToNull(List<List<String>> cells) {
        return cells.stream()
                .map(row -> row.stream()
                        .map(s -> s.isEmpty() ? null : s)
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
    }
}
