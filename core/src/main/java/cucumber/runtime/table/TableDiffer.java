package cucumber.runtime.table;

import com.google.common.collect.Sets;
import cucumber.api.DataTable;
import cucumber.deps.difflib.Delta;
import cucumber.deps.difflib.DiffUtils;
import cucumber.deps.difflib.Patch;
import gherkin.formatter.model.DataTableRow;
import gherkin.formatter.model.Row;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TableDiffer {

    private final DataTable from;
    private final DataTable to;

    public TableDiffer(DataTable fromTable, DataTable toTable) {
        checkColumns(fromTable, toTable);
        this.from = fromTable;
        this.to = toTable;
    }

    private void checkColumns(DataTable a, DataTable b) {
        if (a.topCells().size() != b.topCells().size()) {
            throw new IllegalArgumentException("Tables must have equal number of columns:\n" + a + "\n" + b);
        }
    }

    public void calculateDiffs() throws TableDiffException {
        Patch patch = DiffUtils.diff(from.diffableRows(), to.diffableRows());
        List<Delta> deltas = patch.getDeltas();
        if (!deltas.isEmpty()) {
            Map<Integer, Delta> deltasByLine = createDeltasByLine(deltas);
            throw new TableDiffException(from, to, createTableDiff(deltasByLine));
        }
    }

    public void calculateDiffsSet() throws TableDiffException {
        Set<List<String>> rowsSet = Sets.newHashSet(from.raw());
        Set<List<String>> otherRowsSet = Sets.newHashSet(to.raw());

        // missing rows from origin
        Set<List<String>> missingRows = Sets.difference(rowsSet, otherRowsSet);

        // missing rows
        Set<List<String>> extraRows = Sets.difference(otherRowsSet, rowsSet);


        if (!missingRows.isEmpty() || !extraRows.isEmpty()){
            throw new TableDiffException(from, to, createTableDiffSet(missingRows, extraRows));
        }
    }

    private Map<Integer, Delta> createDeltasByLine(List<Delta> deltas) {
        Map<Integer, Delta> deltasByLine = new HashMap<Integer, Delta>();
        for (Delta delta : deltas) {
            deltasByLine.put(delta.getOriginal().getPosition(), delta);
        }
        return deltasByLine;
    }

    private DataTable createTableDiff(Map<Integer, Delta> deltasByLine) {
        List<DataTableRow> diffTableRows = new ArrayList<DataTableRow>();
        List<List<String>> rows = from.raw();
        for (int i = 0; i < rows.size(); i++) {
            Delta delta = deltasByLine.get(i);
            if (delta == null) {
                diffTableRows.add(from.getGherkinRows().get(i));
            } else {
                addRowsToTableDiff(diffTableRows, delta);
                // skipping lines involved in a delta
                if (delta.getType() == Delta.TYPE.CHANGE || delta.getType() == Delta.TYPE.DELETE) {
                    i += delta.getOriginal().getLines().size() - 1;
                } else {
                    diffTableRows.add(from.getGherkinRows().get(i));
                }
            }
        }
        // Can have new lines at end
        Delta remainingDelta = deltasByLine.get(rows.size());
        if (remainingDelta != null) {
            addRowsToTableDiff(diffTableRows, remainingDelta);
        }
        return new DataTable(diffTableRows, from.getTableConverter());
    }

    private void addRowsToTableDiff(List<DataTableRow> diffTableRows, Delta delta) {
        markChangedAndDeletedRowsInOriginalAsMissing(diffTableRows, delta);
        markChangedAndInsertedRowsInRevisedAsNew(diffTableRows, delta);
    }

    private void markChangedAndDeletedRowsInOriginalAsMissing(List<DataTableRow> diffTableRows, Delta delta) {
        List<DiffableRow> deletedLines = (List<DiffableRow>) delta.getOriginal().getLines();
        for (DiffableRow row : deletedLines) {
            diffTableRows.add(new DataTableRow(row.row.getComments(), row.row.getCells(), row.row.getLine(), Row.DiffType.DELETE));
        }
    }

    private void markChangedAndInsertedRowsInRevisedAsNew(List<DataTableRow> diffTableRows, Delta delta) {
        List<DiffableRow> insertedLines = (List<DiffableRow>) delta.getRevised().getLines();
        for (DiffableRow row : insertedLines) {
            diffTableRows.add(new DataTableRow(row.row.getComments(), row.row.getCells(), row.row.getLine(), Row.DiffType.INSERT));
        }
    }

    private DataTable createTableDiffSet(Set<List<String>> rowDiffOther, Set<List<String>> extraRows) {
        List<DataTableRow> diffTableRows = new ArrayList<DataTableRow>();

        // iterate over from to detect missing rows
        for (int i = 0; i < from.raw().size(); i++) {
            DataTableRow currentGerkinRow = from.getGherkinRows().get(i);
            if (rowDiffOther.contains(from.raw().get(i))) {
                diffTableRows.add(
                        new DataTableRow(currentGerkinRow.getComments(),
                                currentGerkinRow.getCells(),
                                currentGerkinRow.getLine(),
                                Row.DiffType.DELETE));
            } else {
                diffTableRows.add(from.getGherkinRows().get(i));
            }
        }

        // iterate over to, to detect extra rows
        for (int i = 0; i < to.raw().size(); i++) {
            DataTableRow currentGerkinRow = to.getGherkinRows().get(i);
            if (extraRows.contains(to.raw().get(i))) {
                diffTableRows.add(
                        new DataTableRow(currentGerkinRow.getComments(),
                                currentGerkinRow.getCells(),
                                currentGerkinRow.getLine(),
                                Row.DiffType.INSERT));
            }
        }
        return new DataTable(diffTableRows, from.getTableConverter());
    }
}
