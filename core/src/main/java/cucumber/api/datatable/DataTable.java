package cucumber.api.datatable;

import cucumber.runtime.CucumberException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

public final class DataTable {

    private final List<List<String>> raw;
    private final TableConverter tableConverter;

    public static DataTable emptyDataTable() {
        return create(Collections.<List<String>>emptyList());
    }

    public static DataTable create(List<List<String>> raw) {
        return new DataTable(raw, new NoConverterDefined());
    }

    /**
     * Creates a new DataTable. This constructor should not be called by Cucumber users - it's used internally only.
     *
     * @param raw            the underlying table.
     * @param tableConverter how to convert the rows.
     */
    DataTable(List<List<String>> raw, TableConverter tableConverter) {
        if (raw == null) throw new CucumberDataTableException("cells can not be null");
        if (tableConverter == null) throw new CucumberDataTableException("tableConverter can not be null");
        this.raw = copyAndRequireBalancedTable(raw);
        this.tableConverter = tableConverter;
    }

    private static List<List<String>> copyAndRequireBalancedTable(List<List<String>> table) {
        int columns = table.isEmpty() ? 0 : table.get(0).size();

        List<List<String>> rawCopy = new ArrayList<List<String>>(table.size());
        for (List<String> row : table) {
            if (columns != row.size()) {
                throw new CucumberException(String.format("Table is unbalanced: expected %s column(s) but found %s.", columns, row.size()));
            }
            List<String> rowCopy = new ArrayList<String>(row.size());
            rowCopy.addAll(row);
            rawCopy.add(rowCopy);
        }
        return rawCopy;
    }

    /**
     * @return the raw modifiable backing table
     */
    List<List<String>> raw() {
        return raw;
    }

    /**
     * Converts the table to a list of maps of strings. The top row is used as keys in the maps,
     * and the rows below are used as values.
     *
     * @return a list of maps.
     */
    public List<Map<String, String>> asMaps() {
        if (raw.isEmpty()) return emptyList();

        List<String> headers = raw.get(0);
        List<Map<String, String>> headersAndRows = new ArrayList<Map<String, String>>();

        for (int i = 1; i < raw.size(); i++) {
            List<String> row = raw.get(i);
            LinkedHashMap<String, String> headersAndRow = new LinkedHashMap<String, String>();
            for (int j = 0; j < headers.size(); j++) {
                headersAndRow.put(headers.get(j), row.get(j));
            }
            headersAndRows.add(unmodifiableMap(headersAndRow));
        }

        return unmodifiableList(headersAndRows);
    }


    /**
     * Converts the table to a list of maps. The top row is used as keys in the maps,
     * and the rows below are used as values.
     *
     * @param <K>       key type
     * @param <V>       value type
     * @param keyType   key type
     * @param valueType value type
     * @return a list of maps.
     */
    public <K, V> List<Map<K, V>> asMaps(Type keyType, Type valueType) {
        return tableConverter.toMaps(this, keyType, valueType);
    }

    /**
     * Converts the table to a single map. The left column is used as keys, the right column as values.
     *
     * @param <K>       key type
     * @param <V>       value type
     * @param keyType   key type
     * @param valueType value type
     * @return a Map.
     * @throws cucumber.runtime.CucumberException if the table doesn't have 2 columns.
     */
    public <K, V> Map<K, V> asMap(Type keyType, Type valueType) {
        return tableConverter.toMap(this, keyType, valueType);
    }

    /**
     * Converts the table to a list of list of string.
     *
     * @return a List of List of objects
     */
    public List<List<String>> asLists() {
        return cells();
    }

    /**
     * Converts the table to a List of List of scalar.
     *
     * @param itemType the type of the list items
     * @param <T>      the type of the list items
     * @return a List of List of objects
     */
    public <T> List<List<T>> asLists(Type itemType) {
        return tableConverter.toLists(this, itemType);
    }


    /**
     * Converts the table to a List.
     * <p>
     * If {@code itemType} is a scalar type the table is flattened.
     * <p>
     * Otherwise, the top row is used to name the fields/properties and the remaining
     * rows are turned into list items.
     *
     * @param itemType the type of the list items
     * @param <T>      the type of the list items
     * @return a List of objects
     */
    public <T> List<T> asList(Type itemType) {
        return tableConverter.toList(this, itemType);
    }


    public List<List<String>> cells() {
        List<List<String>> rawCopy = new ArrayList<List<String>>(raw.size());
        for (List<String> row : raw) {
            List<String> rowCopy = new ArrayList<String>(row.size());
            rowCopy.addAll(row);
            rawCopy.add(unmodifiableList(rowCopy));
        }
        return unmodifiableList(rawCopy);
    }

    public List<String> topCells() {
        return raw.isEmpty() ? Collections.<String>emptyList() : unmodifiableList(raw.get(0));
    }

    public List<List<String>> rows(int fromRow) {
        return rows(fromRow, raw.size());
    }

    public List<List<String>> rows(int fromRow, int toRow) {
        return raw.subList(fromRow, toRow);
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
     *
     * @param other the other table to diff with.
     * @throws TableDiffException if the tables are different.
     */
    public void unorderedDiff(DataTable other) throws TableDiffException {
        new TableDiffer(this, other).calculateUnorderedDiffs();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        print(result);
        return result.toString();
    }

    public void print(StringBuilder appendable) {
        TablePrinter printer = new TablePrinter();
        printer.printTable(raw, appendable);
    }

    public void print(Appendable appendable) throws IOException {
        TablePrinter printer = new TablePrinter();
        printer.printTable(raw, appendable);
    }

    public DataTable transpose() {
        List<List<String>> transposed = new ArrayList<List<String>>();
        for (List<String> pickleRow : raw) {
            for (int j = 0; j < pickleRow.size(); j++) {
                List<String> row = null;
                if (j < transposed.size()) {
                    row = transposed.get(j);
                }
                if (row == null) {
                    row = new ArrayList<String>();
                    transposed.add(row);
                }
                row.add(pickleRow.get(j));
            }
        }
        return new DataTable(transposed, tableConverter);
    }

    public <T> T convert(Type type, boolean transposed) {
        return tableConverter.convert(this, type, transposed);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataTable dataTable = (DataTable) o;

        return raw.equals(dataTable.raw);
    }

    public boolean isEmpty() {
        return raw.isEmpty();
    }

    public int width() {
        return raw.isEmpty() ? 0 : raw.get(0).size();
    }

    public int height() {
        return raw.size();
    }


    @Override
    public int hashCode() {
        return raw.hashCode();
    }

    private static final class NoConverterDefined implements TableConverter {

        @Override
        public <T> T convert(DataTable dataTable, Type type, boolean transposed) {
            throw new CucumberDataTableException(String.format("Can't convert DataTable to %s. DataTable was created without a converter", type));
        }

        @Override
        public <T> List<T> toList(DataTable dataTable, Type itemType) {
            throw new CucumberDataTableException(String.format("Can't convert DataTable to List<%s>. DataTable was created without a converter", itemType));
        }

        @Override
        public <T> List<List<T>> toLists(DataTable dataTable, Type itemType) {
            throw new CucumberDataTableException(String.format("Can't convert DataTable to List<List<%s>>. DataTable was created without a converter", itemType));
        }

        @Override
        public <K, V> Map<K, V> toMap(DataTable dataTable, Type keyType, Type valueType) {
            throw new CucumberDataTableException(String.format("Can't convert DataTable to Map<%s,%s>. DataTable was created without a converter", keyType, valueType));
        }

        @Override
        public <K, V> List<Map<K, V>> toMaps(DataTable dataTable, Type keyType, Type valueType) {
            throw new CucumberDataTableException(String.format("Can't convert DataTable to List<Map<%s,%s>>. DataTable was created without a converter", keyType, valueType));
        }

        @Override
        public DataTable toTable(List<?> objects, String... columnNames) {
            throw new CucumberDataTableException("Can't create a DataTable. DataTable was created without a converter");
        }

    }
}