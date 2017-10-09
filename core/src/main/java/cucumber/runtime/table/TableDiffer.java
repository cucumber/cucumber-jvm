package cucumber.runtime.table;

import cucumber.api.DataTable;
import cucumber.deps.difflib.Delta;
import cucumber.deps.difflib.DiffUtils;
import cucumber.deps.difflib.Patch;
import gherkin.pickles.PickleCell;
import gherkin.pickles.PickleRow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cucumber.runtime.table.DataTableDiff.DiffType;
import static java.util.AbstractMap.SimpleEntry;

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
        List<SimpleEntry<PickleRow, DiffType>> diffTableRows = new ArrayList<SimpleEntry<PickleRow, DiffType>>();
        List<List<String>> missingRow    = new ArrayList<List<String>>();

        ArrayList<List<String>> extraRows = new ArrayList<List<String>>();

        // 1. add all "to" row in extra table
        // 2. iterate over "from", when a common row occurs, remove it from extraRows
        // finally, only extra rows are kept and in same order that in "to".
        extraRows.addAll(to.raw());

        for (PickleRow r : from.getPickleRows()) {
            if (!to.raw().contains(getCellValues(r))) {
                missingRow.add(getCellValues(r));
                diffTableRows.add(
                        new SimpleEntry<PickleRow, DiffType>(new PickleRow(r.getCells()), DiffType.DELETE));
                isDifferent = true;
            } else {
                diffTableRows.add(
                        new SimpleEntry<PickleRow, DiffType>(new PickleRow(r.getCells()), DiffType.NONE));
                extraRows.remove(getCellValues(r));
            }
        }

        for (List<String> e : extraRows) {
            diffTableRows.add(
                    new SimpleEntry<PickleRow, DiffType>(new PickleRow(convertToPickleCells(e)), DiffType.INSERT));
            isDifferent = true;
        }

        if (isDifferent) {
            throw new TableDiffException(from, to, DataTableDiff.create(diffTableRows, from.getTableConverter()));
        }
    }

    private List<PickleCell> convertToPickleCells(List<String> e) {
        List<PickleCell> cells = new ArrayList<PickleCell>(e.size());
        for (String value : e) {
            cells.add(new PickleCell(null, value));
        }
        return cells;
    }

    private List<String> getCellValues(PickleRow r) {
        List<String> values = new ArrayList<String>(r.getCells().size());
        for (PickleCell cell : r.getCells()) {
            values.add(cell.getValue());
        }
        return values;
    }

    private Map<Integer, Delta> createDeltasByLine(List<Delta> deltas) {
        Map<Integer, Delta> deltasByLine = new HashMap<Integer, Delta>();
        for (Delta delta : deltas) {
            deltasByLine.put(delta.getOriginal().getPosition(), delta);
        }
        return deltasByLine;
    }

    private DataTable createTableDiff(Map<Integer, Delta> deltasByLine) {
        List<SimpleEntry<PickleRow, DiffType>> diffTableRows = new ArrayList<SimpleEntry<PickleRow, DiffType>>();
        List<List<String>> rows = from.raw();
        for (int i = 0; i < rows.size(); i++) {
            Delta delta = deltasByLine.get(i);
            if (delta == null) {
                diffTableRows.add(new SimpleEntry<PickleRow, DiffType>(from.getPickleRows().get(i), DiffType.NONE));
            } else {
                addRowsToTableDiff(diffTableRows, delta);
                // skipping lines involved in a delta
                if (delta.getType() == Delta.TYPE.CHANGE || delta.getType() == Delta.TYPE.DELETE) {
                    i += delta.getOriginal().getLines().size() - 1;
                } else {
                    diffTableRows.add(new SimpleEntry<PickleRow, DiffType>(from.getPickleRows().get(i), DiffType.NONE));
                }
            }
        }
        // Can have new lines at end
        Delta remainingDelta = deltasByLine.get(rows.size());
        if (remainingDelta != null) {
            addRowsToTableDiff(diffTableRows, remainingDelta);
        }
        return DataTableDiff.create(diffTableRows, from.getTableConverter());
    }

    private void addRowsToTableDiff(List<SimpleEntry<PickleRow, DiffType>> diffTableRows, Delta delta) {
        markChangedAndDeletedRowsInOriginalAsMissing(diffTableRows, delta);
        markChangedAndInsertedRowsInRevisedAsNew(diffTableRows, delta);
    }

    private void markChangedAndDeletedRowsInOriginalAsMissing(List<SimpleEntry<PickleRow, DiffType>> diffTableRows, Delta delta) {
        List<DiffableRow> deletedLines = (List<DiffableRow>) delta.getOriginal().getLines();
        for (DiffableRow row : deletedLines) {
            diffTableRows.add(new SimpleEntry<PickleRow, DiffType>(new PickleRow(row.row.getCells()), DiffType.DELETE));
        }
    }

    private void markChangedAndInsertedRowsInRevisedAsNew(List<SimpleEntry<PickleRow, DiffType>> diffTableRows, Delta delta) {
        List<DiffableRow> insertedLines = (List<DiffableRow>) delta.getRevised().getLines();
        for (DiffableRow row : insertedLines) {
            diffTableRows.add(new SimpleEntry<PickleRow, DiffType>(new PickleRow(row.row.getCells()), DiffType.INSERT));
        }
    }
}
