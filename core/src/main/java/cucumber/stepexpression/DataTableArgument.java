package cucumber.stepexpression;

import java.util.List;

public class DataTableArgument implements Argument {


    private final RawTableTransformer<?> tableType;
    private final List<List<String>> argument;

    DataTableArgument(RawTableTransformer<?> tableType, List<List<String>> argument) {
        this.tableType = tableType;
        this.argument = argument;
    }

    @Override
    public Object getValue() {
        return tableType.transform(argument);
    }
}
