package io.cucumber.core.stepexpression;

import io.cucumber.core.feature.CucumberStep;
import io.cucumber.core.feature.DataTableArgument;
import io.cucumber.core.feature.DocStringArgument;

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

public final class ArgumentMatcher {

    private final StepExpression expression;

    public ArgumentMatcher(StepExpression expression) {
        this.expression = expression;
    }

    public List<Argument> argumentsFrom(CucumberStep step, Type... types) {
        io.cucumber.core.feature.Argument arg = step.getArgument();
        if (arg == null) {
            return expression.match(step.getText(), types);
        }

        if (arg instanceof DocStringArgument) {
            DocStringArgument docString = (DocStringArgument) arg;
            String content = docString.getContent();
            return expression.match(step.getText(), content, types);
        }

        if (arg instanceof DataTableArgument) {
            DataTableArgument table = (DataTableArgument) arg;
            List<List<String>> cells = emptyCellsToNull(table.cells());
            return expression.match(step.getText(), cells, types);
        }

        throw new IllegalStateException("Argument was neither PickleString nor PickleTable");
    }

    private List<List<String>> emptyCellsToNull(List<List<String>> cells) {
        return cells.stream()
            .map(row -> row.stream()
                .map(s -> s.isEmpty() ? null : s)
                .collect(Collectors.toList()))
            .collect(Collectors.toList());
    }

}
