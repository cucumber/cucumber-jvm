package cucumber.api;

import cucumber.runtime.CucumberException;
import cucumber.runtime.ParameterInfo;
import cucumber.runtime.table.DiffableRow;
import cucumber.runtime.table.TableConverter;
import cucumber.runtime.table.TableDiffException;
import cucumber.runtime.table.TableDiffer;
import cucumber.runtime.table.TablePrinter;
import cucumber.runtime.xstream.LocalizedXStreams;
import gherkin.pickles.PickleCell;
import gherkin.pickles.PickleRow;
import gherkin.pickles.PickleTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Represents the data from a <a href="http://cucumber.info/gherkin.html#data-tables">Gherkin DataTable</a>. Cucumber will convert the table in Gherkin
 * to a DataTable instance and pass it to a step definition.
 */
public class DataTable {

    private final List<List<String>> raw;
    private final PickleTable pickleTable;
    private final TableConverter tableConverter;

    public static DataTable create(List<?> raw) {
        return create(raw, Locale.getDefault(), null, new String[0]);
    }

    public static DataTable create(List<?> raw, String format, String... columnNames) {
        return create(raw, Locale.getDefault(), format, columnNames);
    }

    public static DataTable create(List<?> raw, Locale locale, String... columnNames) {
        return create(raw, locale, null, columnNames);
    }

    private static DataTable create(List<?> raw, Locale locale, String format, String... columnNames) {
        ParameterInfo parameterInfo = new ParameterInfo(null, format, null, null);
        TableConverter tableConverter = new TableConverter(new LocalizedXStreams(Thread.currentThread().getContextClassLoader()).get(locale), parameterInfo);
        return tableConverter.toTable(raw, columnNames);
    }

    /**
     * Creates a new DataTable. This constructor should not be called by Cucumber users - it's used internally only.
     *
     * @param pickleTable    the underlying table.
     * @param tableConverter how to convert the rows.
     */
    public DataTable(PickleTable pickleTable, TableConverter tableConverter) {
        this.pickleTable = pickleTable;
        this.tableConverter = tableConverter;
        int columns = pickleTable.getRows().isEmpty() ? 0 : pickleTable.getRows().get(0).getCells().size();
        List<List<String>> raw = new ArrayList<List<String>>();
        for (PickleRow row : pickleTable.getRows()) {
            List<String> list = new ArrayList<String>();
            for (PickleCell cell : row.getCells()) {
                list.add(cell.getValue());
            }
            if (columns != row.getCells().size()) {
                throw new CucumberException(String.format("Table is unbalanced: expected %s column(s) but found %s.", columns, row.getCells().size()));
            }
            raw.add(Collections.unmodifiableList(list));
        }
        this.raw = Collections.unmodifiableList(raw);
    }

    private DataTable(PickleTable pickleTable, List<List<String>> raw, TableConverter tableConverter) {
        this.pickleTable = pickleTable;
        this.tableConverter = tableConverter;
        this.raw = Collections.unmodifiableList(raw);
    }

    /**
     * @return a List of List of String.
     */
    public List<List<String>> raw() {
        return this.raw;
    }

    /**
     * Converts the table to a List of Map. The top row is used as keys in the maps,
     * and the rows below are used as values.
     *
     * @param <K> key type
     * @param <V> value type
     * @param keyType key type
     * @param valueType value type
     *
     * @return a List of Map.
     */
    public <K, V> List<Map<K, V>> asMaps(Class<K> keyType, Class<V> valueType) {
        return tableConverter.toMaps(this, keyType, valueType);
    }

    /**
     * Converts the table to a single Map. The left column is used as keys, the right column as values.
     *
     * @param <K> key type
     * @param <V> value type
     * @param keyType key type
     * @param valueType value type
     * @return a Map.
     * @throws cucumber.runtime.CucumberException if the table doesn't have 2 columns.
     */
    public <K, V> Map<K, V> asMap(Class<K> keyType, Class<V> valueType) {
        return tableConverter.toMap(this, keyType, valueType);
    }

    /**
     * Converts the table to a List.
     *
     * If {@code itemType} is a scalar type the table is flattened.
     *
     * Otherwise, the top row is used to name the fields/properties and the remaining
     * rows are turned into list items.
     *
     * @param itemType the type of the list items
     * @param <T>      the type of the list items
     * @return a List of objects
     */
    public <T> List<T> asList(Class<T> itemType) {
        return tableConverter.toList(this, itemType);
    }

    /**
     * Converts the table to a List of List of scalar.
     *
     * @param itemType the type of the list items
     * @param <T>      the type of the list items
     * @return a List of List of objects
     */
    public <T> List<List<T>> asLists(Class<T> itemType) {
        return tableConverter.toLists(this, itemType);
    }

    public List<String> topCells() {
        return raw.isEmpty() ? Collections.<String>emptyList() : raw.get(0);
    }

    public List<List<String>> cells(int firstRow) {
        return raw.subList(firstRow, raw.size());
    }

    /**
     * Creates another table using the same {@link Locale} and {@link Format} that was used to create this table.
     *
     * @param raw         a list of objects
     * @param columnNames optional explicit header columns
     * @return a new table
     */
    public DataTable toTable(List<?> raw, String... columnNames) {
        return tableConverter.toTable(raw, columnNames);
    }

    /**
     * Diffs this table with {@code other}, which can be a {@code List&lt;List&lt;String&gt;&gt;} or a
     * {@code List&lt;YourType&gt;}.
     *
     * @param other the other table to diff with.
     * @throws cucumber.runtime.table.TableDiffException if the tables are different.
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

    /**
     * Diffs this table with {@code other}.
     * The order is not important. A set-difference is applied.
     * @param other the other table to diff with.
     * @throws TableDiffException if the tables are different.
     */
    public void unorderedDiff(DataTable other) throws TableDiffException {
        new TableDiffer(this, other).calculateUnorderedDiffs();
    }

    /**
     * Diffs this table with {@code other}, which can be a {@code List&lt;List&lt;String&gt;&gt;} or a
     * {@code List&lt;YourType&gt;}.
     *
     * @param other the other table to diff with.
     * @throws cucumber.runtime.table.TableDiffException if the tables are different.
     */
    public void unorderedDiff(List<?> other) throws TableDiffException {
        List<String> topCells = topCells();
        DataTable otherTable = toTable(other, topCells.toArray(new String[topCells.size()]));
        unorderedDiff(otherTable);
    }

    /**
     * Internal method. Do not use.
     *
     * @return a list of raw rows.
     */
    public List<PickleRow> getPickleRows() {
        return Collections.unmodifiableList(pickleTable.getRows());
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        TablePrinter printer = createTablePrinter();
        printer.printTable(raw, result);
        return result.toString();
    }

    public List<DiffableRow> diffableRows() {
        List<DiffableRow> result = new ArrayList<DiffableRow>();
        List<List<String>> convertedRows = raw();
        for (int i = 0; i < convertedRows.size(); i++) {
            result.add(new DiffableRow(getPickleRows().get(i), convertedRows.get(i)));
        }
        return result;
    }

    public TableConverter getTableConverter() {
        return tableConverter;
    }

    public DataTable transpose() {
        List<List<String>> transposed = new ArrayList<List<String>>();
        for (int i = 0; i < pickleTable.getRows().size(); i++) {
            PickleRow pickleRow = pickleTable.getRows().get(i);
            for (int j = 0; j < pickleRow.getCells().size(); j++) {
                List<String> row = null;
                if (j < transposed.size()) {
                    row = transposed.get(j);
                }
                if (row == null) {
                    row = new ArrayList<String>();
                    transposed.add(row);
                }
                row.add(pickleRow.getCells().get(j).getValue());
            }
        }
        return new DataTable(this.pickleTable, transposed, this.tableConverter);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataTable)) return false;

        DataTable dataTable = (DataTable) o;

        if (!raw.equals(dataTable.raw)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return raw.hashCode();
    }

    protected TablePrinter createTablePrinter() {
        return new TablePrinter();
    }
}
