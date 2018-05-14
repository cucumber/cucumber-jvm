package cucumber.runtime;

import cucumber.api.Argument;
import io.cucumber.stepexpression.ExpressionArgument;

import java.util.ArrayList;
import java.util.List;

public final class DefinitionArgument implements Argument {

    private final io.cucumber.cucumberexpressions.Group group;

    private DefinitionArgument(ExpressionArgument expressionArgument) {
        group = expressionArgument.getGroup();
    }

    public static List<Argument> createArguments(List<io.cucumber.stepexpression.Argument> match) {
        List<Argument> args = new ArrayList<Argument>();
        for (io.cucumber.stepexpression.Argument argument : match) {
            if (argument instanceof ExpressionArgument) {
                args.add(new DefinitionArgument((ExpressionArgument) argument));
            }
        }
        return args;
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
