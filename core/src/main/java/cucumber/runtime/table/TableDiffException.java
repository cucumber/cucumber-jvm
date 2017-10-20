package cucumber.runtime.table;

import cucumber.api.DataTable;

public class TableDiffException extends RuntimeException {
    private final DataTable from;
    private final DataTable to;
    private final DataTableDiff diff;

    public TableDiffException(DataTable from, DataTable to, DataTableDiff diff) {
        super("Tables were not identical:\n" + diff.toString());
        this.from = from;
        this.to = to;
        this.diff = diff;
    }

    /**
     * @return the left side of the diff
     */
    public DataTable getFrom() {
        return from;
    }

    /**
     * @return the right side of the diff
     */
    public DataTable getTo() {
        return to;
    }

    /**
     * @return the diff itself - represented as a table
     */
    public DataTableDiff getDiff() {
        return diff;
    }
}
