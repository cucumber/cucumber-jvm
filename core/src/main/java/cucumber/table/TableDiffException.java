package cucumber.table;

public class TableDiffException extends RuntimeException {
    public TableDiffException(DataTable tableDiff) {
        super("Tables were not identical:\n" + tableDiff.toString());
    }
}
