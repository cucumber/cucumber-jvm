package cucumber.table;

public class TableDiffException extends RuntimeException {

    private DataTable tableDiff;

    public TableDiffException(DataTable tableDiff) {
        super("Tables were not identical");
        this.tableDiff = tableDiff;
    }

    public DataTable getDiffTable() {
        return this.tableDiff;
    }

}
