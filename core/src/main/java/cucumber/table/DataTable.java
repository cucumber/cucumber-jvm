package cucumber.table;

import cucumber.runtime.converters.LocalizedXStreams;
import gherkin.formatter.PrettyFormatter;
import gherkin.formatter.model.DataTableRow;
import gherkin.formatter.model.Row;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DataTable {

    private final List<List<String>> raw;
    private final List<DataTableRow> gherkinRows;
    private final TableConverter tableConverter;

    public static DataTable create(List<?> raw) {
        return create(raw, Locale.getDefault(), null, new String[0]);
    }

    public static DataTable create(List<?> raw, String dateFormat, String... columnNames) {
        return create(raw, Locale.getDefault(), dateFormat, columnNames);
    }

    public static DataTable create(List<?> raw, Locale locale, String... columnNames) {
        return create(raw, locale, null, columnNames);
    }

    public static DataTable create(List<?> raw, Locale locale, String dateFormat, String... columnNames) {
        TableConverter tableConverter = new TableConverter(new LocalizedXStreams(Thread.currentThread().getContextClassLoader()).get(locale), dateFormat);
        return tableConverter.toTable(raw, columnNames);
    }

    public DataTable(List<DataTableRow> gherkinRows, TableConverter tableConverter) {
        this.gherkinRows = gherkinRows;
        this.tableConverter = tableConverter;
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

    public <T> List<T> asList(Type listType) {
        return tableConverter.toList(listType, this);
    }

    List<String> topCells() {
        return raw.get(0);
    }

    List<List<String>> cells(int firstRow) {
        List<List<String>> attributeValues = new ArrayList<List<String>>();
        List<DataTableRow> valueRows = gherkinRows.subList(firstRow, gherkinRows.size());
        for (Row valueRow : valueRows) {
            attributeValues.add(toStrings(valueRow));
        }
        return attributeValues;
    }

    private List<String> toStrings(Row row) {
        List<String> strings = new ArrayList<String>();
        for (String string : row.getCells()) {
            strings.add(string);
        }
        return strings;
    }

    /**
     * Creates another table using the same {@link Locale} and {@link cucumber.DateFormat} that was used to create this table.
     *
     * @param raw         a list of objects (Pojos)
     * @param columnNames optional explicit header columns
     * @return
     */
    public DataTable toTable(List<?> raw, String... columnNames) {
        return tableConverter.toTable(raw, columnNames);
    }

    /**
     * Diffs this table with {@code other}, which can be a {@code List<List<String>>} or a
     * {@code List<YourType>}.
     *
     * @param other the other table to diff with.
     * @throws TableDiffException if the tables are different.
     */
    public void diff(List<?> other) throws TableDiffException {
        List<String> topCells = topCells();
        DataTable otherTable = toTable(other, topCells.toArray(new String[topCells.size()]));
        diff(otherTable);
    }

    /**
     * Diffs this table with {@code other}.
     *
     * @param other the other table to diff with.
     * @throws TableDiffException if the tables are different.
     */
    public void diff(DataTable other) throws TableDiffException {
        new TableDiffer(this, other).calculateDiffs();
    }

    public List<DataTableRow> getGherkinRows() {
        return gherkinRows;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        PrettyFormatter pf = new PrettyFormatter(result, true, false);
        pf.table(getGherkinRows());
        pf.eof();
        return result.toString();
    }

    List<DiffableRow> diffableRows() {
        List<DiffableRow> result = new ArrayList<DiffableRow>();
        List<List<String>> convertedRows = raw();
        for (int i = 0; i < convertedRows.size(); i++) {
            result.add(new DiffableRow(getGherkinRows().get(i), convertedRows.get(i)));
        }
        return result;
    }

    TableConverter getTableConverter() {
        return tableConverter;
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
