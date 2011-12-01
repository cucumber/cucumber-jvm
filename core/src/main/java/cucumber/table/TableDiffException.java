package cucumber.table;

import static cucumber.table.TablePrinter.pretty;

public class TableDiffException extends RuntimeException {
    public TableDiffException(DataTable tableDiff) {
        super("Tables were not identical:\n" + pretty(tableDiff));
    }
}
