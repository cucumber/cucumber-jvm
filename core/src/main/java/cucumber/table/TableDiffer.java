package cucumber.table;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;
import gherkin.formatter.model.Row;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableDiffer {

    private final Table orig;
    private final Table other;

    public TableDiffer(Table origTable, Table otherTable) {
        this.orig = origTable;
        this.other = otherTable;
    }

    public void calculateDiffs() {
        Patch patch = DiffUtils.diff(orig.diffableRows(), other.diffableRows());
        List<Delta> deltas = patch.getDeltas();
        if (!deltas.isEmpty()) {
            Map<Integer, Delta> deltasByLine = createDeltasByLine(deltas);
            throw new TableDiffException(createTableDiff(deltasByLine));
        }
    }

    private Table createTableDiff(Map<Integer, Delta> deltasByLine) {
        List<Row> diffTableRows = new ArrayList<Row>();
        List<List<String>> rows = orig.raw();
        for (int i = 0; i < rows.size(); i++) {
            Delta delta = deltasByLine.get(i);
            if (delta == null) {
                diffTableRows.add(orig.getGherkinRows().get(i));
            } else {
                i += addRowsToTableDiffAndReturnNumberOfRows(diffTableRows, delta);
            }
        }
        // Can have new lines at end
        Delta remainingDelta = deltasByLine.get(rows.size());
        if (remainingDelta != null) {
            addRowsToTableDiffAndReturnNumberOfRows(diffTableRows, remainingDelta);
        }
        return new Table(diffTableRows);
    }

    private int addRowsToTableDiffAndReturnNumberOfRows(List<Row> diffTableRows, Delta delta) {
        if (delta.getType() == Delta.TYPE.CHANGE || delta.getType() == Delta.TYPE.DELETE) {
            List<Table.DiffableRow> deletedLines = (List<Table.DiffableRow>) delta.getOriginal().getLines();
            for (Table.DiffableRow row : deletedLines) {
                diffTableRows.add(new Row(row.row.getComments(), row.row.getCells(), row.row.getLine(), Row.DiffType.DELETE));
            }
        }
        List<Table.DiffableRow> insertedLines = (List<Table.DiffableRow>) delta.getRevised().getLines();
        for (Table.DiffableRow row : insertedLines) {
            diffTableRows.add(new Row(row.row.getComments(), row.row.getCells(), row.row.getLine(), Row.DiffType.INSERT));
        }
        return delta.getOriginal().getLines().size() - 1;
    }

    private Map<Integer, Delta> createDeltasByLine(List<Delta> deltas) {
        Map<Integer, Delta> deltasByLine = new HashMap<Integer, Delta>();
        for (Delta delta : deltas) {
            deltasByLine.put(delta.getOriginal().getPosition(), delta);
        }
        return deltasByLine;
    }
}
