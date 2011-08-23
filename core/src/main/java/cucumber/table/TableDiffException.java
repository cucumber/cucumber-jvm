package cucumber.table;

public class TableDiffException extends RuntimeException {
    
    private TableDiff tableDiff;
    
    public TableDiffException(TableDiff tableDiff) {
        super("Tables were not identical");
        this.tableDiff = tableDiff;
    }

    public TableDiff getTableDiff() {
        return this.tableDiff;
    }
    
}
