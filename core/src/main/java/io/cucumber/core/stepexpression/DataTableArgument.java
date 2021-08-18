package io.cucumber.core.stepexpression;

import io.cucumber.datatable.DataTable;
import io.cucumber.datatable.DataTableFormatter;

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

    @Override
    public String toString() {
        return "Table:\n" + getText();
    }

    private String getText() {
        return DataTableFormatter.builder()
                .prefixRow("      ")
                .build()
                .format(DataTable.create(argument));
    }

}
