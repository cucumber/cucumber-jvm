package cucumber.table;

public class TableDiffException extends RuntimeException {
    
    private Table tableDiff;
    
    public TableDiffException(Table tableDiff) {
        super("Tables were not identical");
        this.tableDiff = tableDiff;
    }

    public Table getDiffTable() {
        return this.tableDiff;
    }
    
}
