package cucumber.table;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;

public class TableDiffer {

    private List<Delta> deltas;
    private Map<Integer, Delta> deltasByLine;
    private Table orig;
    private Table other;

    public TableDiffer(Table origTable, Table otherTable) {
        this.orig = origTable;
        this.other = otherTable;
    }

    public void calculateDiffs() {
        Patch patch = DiffUtils.diff(this.orig.rows(), this.other.rows());
        this.deltas = patch.getDeltas();
        if (!this.deltas.isEmpty()) {
            createDeltaMap();
            throw new TableDiffException(createTableDiff());
        }
    }

    private TableDiff createTableDiff() {
        TableDiff tableDiff = new TableDiff();
        List<List<Object>> rows = this.orig.rows();
        for (int i = 0; i < rows.size(); i++) {
            Delta delta = this.deltasByLine.get(i);
            if (delta == null) {
                tableDiff.addRowDiff(new RowDiff(DiffType.NONE, rows.get(i)));
            }  else {
                i+= addRowsToTableDiff(tableDiff, delta);
            }
        }
        // Can have new lines at end
        Delta remainingDelta = this.deltasByLine.get(rows.size());
        if (remainingDelta != null) {
            addRowsToTableDiff(tableDiff, remainingDelta);
        }
        return tableDiff;
    }
    
    /**
     * 
     * @param tableDiff 
     * @param delta
     * @return the number of rows
     */
    private int addRowsToTableDiff(TableDiff tableDiff, Delta delta) {
        if (delta.getType() == Delta.TYPE.CHANGE || delta.getType() == Delta.TYPE.DELETE) {
            List<List<Object>> deletedLines = (List<List<Object>>) delta.getOriginal().getLines();
            for (List<Object> row : deletedLines) {
                tableDiff.addRowDiff(new RowDiff(DiffType.DELETE, row));
            }
        }
        List<List<Object>> insertedLines = (List<List<Object>>) delta.getRevised().getLines();
        for (List<Object> row : insertedLines) {
            tableDiff.addRowDiff(new RowDiff(DiffType.INSERT, row));
        }
        return delta.getOriginal().getLines().size() - 1;
    }

    private void createDeltaMap() {
        this.deltasByLine = new HashMap<Integer, Delta>();
        for (Delta delta : deltas) {
            this.deltasByLine.put(delta.getOriginal().getPosition(), delta);
        }
    }
}
