package io.cucumber.stepexpression;

import cucumber.messages.Pickles.PickleStep;

import java.util.List;

public class ExpressionArgumentMatcher implements ArgumentMatcher {

    private final StepExpression expression;

    public ExpressionArgumentMatcher(StepExpression expression) {
        this.expression = expression;
    }

    @Override
    public List<Argument> argumentsFrom(PickleStep step) {
        if (step.hasDocString()) {
            return expression.match(step.getText(), step.getDocString().getContent());
        }
        if (step.hasDataTable()) {
            return expression.match(step.getText(), PickleTableConverter.toTable(step.getDataTable()));
        }
        return expression.match(step.getText());
    }

}
