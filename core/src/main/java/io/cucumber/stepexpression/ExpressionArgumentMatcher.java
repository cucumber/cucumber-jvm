package io.cucumber.stepexpression;

import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleString;
import gherkin.pickles.PickleTable;

import java.lang.reflect.Type;
import java.util.List;

public class ExpressionArgumentMatcher implements ArgumentMatcher {

    private final StepExpression expression;

    public ExpressionArgumentMatcher(StepExpression expression) {
        this.expression = expression;
    }

    @Override
    public List<Argument> argumentsFrom(PickleStep step, Type... types) {
        if (step.getArgument().isEmpty()) {
            return expression.match(step.getText(), types);
        }

        gherkin.pickles.Argument argument = step.getArgument().get(0);

        if (argument instanceof PickleString) {
            return expression.match(step.getText(), ((PickleString) argument).getContent(), types);
        }

        if (argument instanceof PickleTable) {
            return expression.match(step.getText(), PickleTableConverter.toTable((PickleTable) argument), types);
        }

        throw new IllegalStateException("Argument was neither PickleString nor PickleTable");
    }

}
