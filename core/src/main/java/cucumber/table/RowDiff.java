package cucumber.table;

import java.util.List;

public class RowDiff {
    private List<Object> row;
    private DiffType diffType;
    
    public RowDiff() {
        //
    }
    
    public RowDiff(DiffType diffType, List<Object> row) {
        this.row = row;
        this.diffType = diffType;
    }

    public List<Object> getRow() {
        return this.row;
    }

    public void setRow(List<Object> row) {
        this.row = row;
    }

    public DiffType getDiffType() {
        return this.diffType;
    }

    public void setDiffType(DiffType diffType) {
        this.diffType = diffType;
    }
}
