package cucumber.runtime.table;

import cucumber.api.DataTable;
import cucumber.deps.difflib.Delta;
import cucumber.deps.difflib.DiffUtils;
import cucumber.deps.difflib.Patch;
import gherkin.formatter.model.DataTableRow;
import gherkin.formatter.model.Row;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableDiffer {

    private final DataTable from;
    private final DataTable to;

    public TableDiffer(DataTable fromTable, DataTable toTable) {
        checkColumns(fromTable, toTable);
        this.from = fromTable;
        this.to = toTable;
    }

    private void checkColumns(DataTable a, DataTable b) {
        if (a.topCells().size() != b.topCells().size() && !b.topCells().isEmpty()) {
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

    public void calculateUnorderedDiffs() throws TableDiffException {
        boolean isDifferent = false;
        List<DataTableRow> diffTableRows = new ArrayList<DataTableRow>();
        List<List<String>> missingRow    = new ArrayList<List<String>>();

        ArrayList<List<String>> extraRows = new ArrayList<List<String>>();

        // 1. add all "to" row in extra table
        // 2. iterate over "from", when a common row occurs, remove it from extraRows
        // finally, only extra rows are kept and in same order that in "to".
        extraRows.addAll(to.raw());

        int i = 1;
        for (DataTableRow r : from.getGherkinRows()) {
            if (!to.raw().contains(r.getCells())) {
                missingRow.add(r.getCells());
                diffTableRows.add(
                        new DataTableRow(r.getComments(),
                                r.getCells(),
                                i,
                                Row.DiffType.DELETE));
                isDifferent = true;
            } else {
                diffTableRows.add(
                        new DataTableRow(r.getComments(),
                                r.getCells(),
                                i++));
                extraRows.remove(r.getCells());
            }
        }

        for (List<String> e : extraRows) {
            diffTableRows.add(new DataTableRow(Collections.EMPTY_LIST,
                    e,
                    i++,
                    Row.DiffType.INSERT));
            isDifferent = true;
        }

        if (isDifferent) {
            throw new TableDiffException(from, to, new DataTable(diffTableRows, from.getTableConverter()));
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
}
