package cucumber.api.datatable;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;

class DataTableDiff {
    public enum DiffType {
        NONE, DELETE, INSERT
    }

    private final List<List<String>> table;
    private List<DiffType> diffTypes;

    static DataTableDiff create(List<SimpleEntry<List<String>, DiffType>> diffTableRows) {
        List<DiffType> diffTypes = new ArrayList<DiffType>(diffTableRows.size());
        List<List<String>> table = new ArrayList<List<String>>();

        for (SimpleEntry<List<String>, DiffType> row : diffTableRows) {
            table.add(row.getKey());
            diffTypes.add(row.getValue());
        }
        return new DataTableDiff(table, diffTypes);
    }

    private DataTableDiff(List<List<String>> table, List<DiffType> diffTypes) {
        this.table = table;
        this.diffTypes = diffTypes;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        DiffTablePrinter printer = new DiffTablePrinter(diffTypes);
        printer.printTable(table, result);
        return result.toString();
    }
}
