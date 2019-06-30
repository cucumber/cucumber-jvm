package io.cucumber.core.stepexpression;

import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleString;
import gherkin.pickles.PickleTable;
import org.apiguardian.api.API;

import java.lang.reflect.Type;
import java.util.List;

@API(status = API.Status.STABLE)
public final class ArgumentMatcher {

    private final StepExpression expression;

    public ArgumentMatcher(StepExpression expression) {
        this.expression = expression;
    }

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
