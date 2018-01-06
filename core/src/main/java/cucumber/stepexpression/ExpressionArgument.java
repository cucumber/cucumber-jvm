package cucumber.stepexpression;

import cucumber.api.Argument;
import io.cucumber.cucumberexpressions.Group;

public final class ExpressionArgument implements Argument {

    private final io.cucumber.cucumberexpressions.Argument<?> argument;

    ExpressionArgument(io.cucumber.cucumberexpressions.Argument<?> argument) {
        this.argument = argument;
    }

    @Override
    public Object getValue() {
        return argument.getValue();
    }

    public Group getGroup() {
        return argument.getGroup();
    }
}
