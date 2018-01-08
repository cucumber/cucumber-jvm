package io.cucumber.stepexpression;

import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleString;
import gherkin.pickles.PickleTable;

import java.util.List;

public class ExpressionArgumentMatcher implements ArgumentMatcher {

    private final StepExpression expression;

    public ExpressionArgumentMatcher(StepExpression expression) {
        this.expression = expression;
    }

    @Override
    public List<Argument> argumentsFrom(PickleStep step) {
        if (step.getArgument().isEmpty()) {
            return expression.match(step.getText());
        }

        gherkin.pickles.Argument argument = step.getArgument().get(0);

        if (argument instanceof PickleString) {
            return expression.match(step.getText(), ((PickleString) argument).getContent());
        }

        if (argument instanceof PickleTable) {
            return expression.match(step.getText(), PickleTableConverter.toTable((PickleTable) argument));
        }

        throw new IllegalStateException("Argument was neither PickleString nor PickleTable");
    }

}
