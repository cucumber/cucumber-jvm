package cucumber.stepexpression;

import cucumber.api.datatable.DataTable;

import java.util.List;

public final class DataTableArgument implements Argument {

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

    public String getText() {
        return DataTable.create(argument).toString();
    }

    public List<List<String>> getRawTable(){
        return argument;
    }
}
