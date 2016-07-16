package cucumber.runtime.table;

import cucumber.api.DataTable;
import gherkin.pickles.PickleRow;
import gherkin.pickles.PickleTable;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;

public class DataTableDiff extends DataTable {
    public enum DiffType {
        NONE, DELETE, INSERT
    }

    private List<DiffType> diffTypes;

    public static DataTableDiff create(List<SimpleEntry<PickleRow, DiffType>> diffTableRows, TableConverter tableConverter) {
        List<PickleRow> rows = new ArrayList<PickleRow>(diffTableRows.size());
        List<DiffType> diffTypes = new ArrayList<DiffType>(diffTableRows.size());
        for (SimpleEntry<PickleRow, DiffType> row : diffTableRows) {
            rows.add(row.getKey());
            diffTypes.add(row.getValue());
        }
        return new DataTableDiff(new PickleTable(rows), diffTypes, tableConverter);
    }

    public DataTableDiff(PickleTable pickleTable, List<DiffType> diffTypes, TableConverter tableConverter) {
        super(pickleTable, tableConverter);
        this.diffTypes = diffTypes;

    }

    @Override
    protected TablePrinter createTablePrinter() {
        return new DiffTablePrinter(diffTypes);
    }

}
