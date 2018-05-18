package io.cucumber.stepexpression;

import io.cucumber.datatable.DataTable;

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

    @Override
    public String toString() {
        return "Table:\n" + getText();
    }
}
