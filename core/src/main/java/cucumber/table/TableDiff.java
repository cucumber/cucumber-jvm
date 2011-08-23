package cucumber.table;

import java.util.ArrayList;
import java.util.List;

public class TableDiff {
    private List<RowDiff> rowDiffs;

    public List<RowDiff> getRowDiffs() {
        if (this.rowDiffs == null) {
            this.rowDiffs = new ArrayList<RowDiff>();
        }
        return this.rowDiffs;
    }

    public void setRowDiffs(List<RowDiff> rowDiffs) {
        this.rowDiffs = rowDiffs;
    }
    
    public void addRowDiff(RowDiff rowDiff) {
        getRowDiffs().add(rowDiff);
    }
}
