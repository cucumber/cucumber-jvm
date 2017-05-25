package cucumber.runtime.table;

import gherkin.pickles.PickleRow;

import java.util.List;

public class DiffableRow {
    public final PickleRow row;
    public final List<String> convertedRow;

    public DiffableRow(PickleRow row, List<String> convertedRow) {
        this.row = row;
        this.convertedRow = convertedRow;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiffableRow that = (DiffableRow) o;
        return convertedRow.equals(that.convertedRow);

    }

    @Override
    public int hashCode() {
        return convertedRow.hashCode();
    }
}
