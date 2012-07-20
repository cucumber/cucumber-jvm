package cucumber.table;

import cucumber.difflib.Delta;
import cucumber.difflib.DiffUtils;
import cucumber.difflib.Patch;
import gherkin.formatter.model.DataTableRow;
import gherkin.formatter.model.Row;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableDiffer {

    private final DataTable orig;
    private final DataTable other;

    public TableDiffer(DataTable origTable, DataTable otherTable) {
        checkColumns(origTable, otherTable);
        this.orig = origTable;
        this.other = otherTable;
    }

    private void checkColumns(DataTable a, DataTable b) {
        if (a.topCells().size() != b.topCells().size()) {
            throw new IllegalArgumentException("Tables must have equal number of columns:\n" + a + "\n" + b);
        }
    }

    public void calculateDiffs() throws TableDiffException {
        Patch patch = DiffUtils.diff(orig.diffableRows(), other.diffableRows());
        List<Delta> deltas = patch.getDeltas();
        if (!deltas.isEmpty()) {
            Map<Integer, Delta> deltasByLine = createDeltasByLine(deltas);
            throw new TableDiffException(createTableDiff(deltasByLine));
        }
    }

    private DataTable createTableDiff(Map<Integer, Delta> deltasByLine) {
        List<DataTableRow> diffTableRows = new ArrayList<DataTableRow>();
        List<List<String>> rows = orig.raw();
        for (int i = 0; i < rows.size(); i++) {
            Delta delta = deltasByLine.get(i);
            if (delta == null) {
                diffTableRows.add(orig.getGherkinRows().get(i));
            } else {
                addRowsToTableDiff(diffTableRows, delta);
            }
        }
        // Can have new lines at end
        Delta remainingDelta = deltasByLine.get(rows.size());
        if (remainingDelta != null) {
            addRowsToTableDiff(diffTableRows, remainingDelta);
        }
        return new DataTable(diffTableRows, orig.getTableConverter());
    }

    private void addRowsToTableDiff(List<DataTableRow> diffTableRows, Delta delta) {
        if (delta.getType() == Delta.TYPE.CHANGE || delta.getType() == Delta.TYPE.DELETE) {
            List<DataTable.DiffableRow> deletedLines = (List<DataTable.DiffableRow>) delta.getOriginal().getLines();
            for (DataTable.DiffableRow row : deletedLines) {
                diffTableRows.add(new DataTableRow(row.row.getComments(), row.row.getCells(), row.row.getLine(), Row.DiffType.DELETE));
            }
        }
        List<DataTable.DiffableRow> insertedLines = (List<DataTable.DiffableRow>) delta.getRevised().getLines();
        for (DataTable.DiffableRow row : insertedLines) {
            diffTableRows.add(new DataTableRow(row.row.getComments(), row.row.getCells(), row.row.getLine(), Row.DiffType.INSERT));
        }
    }

    private Map<Integer, Delta> createDeltasByLine(List<Delta> deltas) {
        Map<Integer, Delta> deltasByLine = new HashMap<Integer, Delta>();
        for (Delta delta : deltas) {
            deltasByLine.put(delta.getOriginal().getPosition(), delta);
        }
        return deltasByLine;
    }
}
