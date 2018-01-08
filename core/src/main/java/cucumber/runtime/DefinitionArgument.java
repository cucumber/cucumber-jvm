package cucumber.runtime;

import cucumber.api.Argument;
import io.cucumber.stepexpression.ExpressionArgument;

public final class DefinitionArgument implements Argument {

    private final io.cucumber.cucumberexpressions.Group group;

    public DefinitionArgument(ExpressionArgument expressionArgument) {
        group = expressionArgument.getGroup();
    }

    @Override
    public String getValue() {
        return group == null ? null : group.getValue();
    }

    @Override
    public int getStart() {
        return group == null ? -1 : group.getStart();
    }

    @Override
    public int getEnd() {
        return group == null ? -1 : group.getEnd();
    }
}
