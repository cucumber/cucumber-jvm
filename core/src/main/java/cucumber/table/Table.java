package cucumber.table;

import gherkin.formatter.model.Row;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Table {

    private final List<List<String>> raw;
    private final Locale locale;
    private final List<Row> gherkinRows;

    public Table(List<Row> gherkinRows, Locale locale) {
        this.gherkinRows = gherkinRows;
        this.locale = locale;
        this.raw = new ArrayList<List<String>>();
        for (Row row : gherkinRows) {
            List<String> list = new ArrayList<String>();
            list.addAll(row.getCells());
            this.raw.add(list);
        }
    }

    public List<List<String>> raw() {
        return this.raw;
    }

    public <T> List<T> asList(T type) {
        throw new UnsupportedOperationException("TODO: i9mplement this method and get rid of the hashes() method");
    }

    public void diff(Table other) {
        new TableDiffer(this, other).calculateDiffs();
    }

    public List<Row> getGherkinRows() {
        return gherkinRows;
    }

    public Locale getLocale() {
        return locale;
    }

    List<DiffableRow> diffableRows() {
        List<DiffableRow> result = new ArrayList<DiffableRow>();
        List<List<String>> convertedRows = raw();
        for (int i = 0; i < convertedRows.size(); i++) {
            result.add(new DiffableRow(getGherkinRows().get(i), convertedRows.get(i)));
        }
        return result;
    }

    class DiffableRow {
        public final Row row;
        public final List<String> convertedRow;

        public DiffableRow(Row row, List<String> convertedRow) {
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
}
