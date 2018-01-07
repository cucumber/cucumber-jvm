package cucumber.api.datatable;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.AbstractList;
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
        return create(raw, new NoConverterDefined());
    }

    public static DataTable create(List<List<String>> raw, TableConverter tableConverter) {
        return new DataTable(raw, tableConverter);
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
                throw new CucumberDataTableException(String.format("Table is unbalanced: expected %s column(s) but found %s.", columns, row.size()));
            }
            if (row.isEmpty()) {
                continue;
            }
            List<String> rowCopy = new ArrayList<String>(row.size());
            rowCopy.addAll(row);
            rawCopy.add(unmodifiableList(rowCopy));
        }
        return unmodifiableList(rawCopy);
    }

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
        return raw;
    }


    public String cell(int column, int row) {
        return raw.get(row).get(column);
    }

    List<String> topCells() {
        return raw.isEmpty() ? Collections.<String>emptyList() : unmodifiableList(raw.get(0));
    }

    public List<List<String>> rows(int fromRow) {
        return rows(fromRow, height());
    }

    public List<List<String>> rows(int fromRow, int toRow) {
        return raw.subList(fromRow, toRow);
    }

    public List<List<String>> columns(final int fromColumn) {
        return new ColumnView(fromColumn, width());
    }

    public List<List<String>> columns(final int fromColumn, final int toColumn) {
        return new ColumnView(fromColumn, toColumn);
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


    private final class ColumnView extends AbstractList<List<String>> {
        private final int fromColumn;
        private final int toColumn;

        ColumnView(int fromColumn, int toColumn) {
            this.fromColumn = fromColumn;
            this.toColumn = toColumn;
        }

        @Override
        public List<String> get(int index) {
            return raw.get(index).subList(fromColumn, toColumn);
        }

        @Override
        public int size() {
            return raw.size();
        }
    }

    private static final class NoConverterDefined implements TableConverter {

        NoConverterDefined() {

        }

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

    }

    /**
     * Converts a {@link DataTable} to another type.
     * <p>
     * There are three ways in which a table might be mapped to a certain type. The table converter considers the
     * possible conversions in this order:
     * <ol>
     * <li>
     * Using the whole table to create a single instance.
     * </li>
     * <li>
     * Using individual rows to create a collection of instances. The first row may be used as header.
     * </li>
     * <li>
     * Using individual cells to a create a collection of instances.
     * </li>
     * </ol>
     */
    public interface TableConverter {

        /**
         * Converts a {@link DataTable} to another type.
         * <p>
         * Delegates to <code>toList</code>, <code>toLists</code>, <code>toMap</code> and <code>toMaps</code>
         * for <code>List&lt;T&gt;</code>, <code>List&lt;List&lt;T&gt;&gt;</code>, <code>Map&lt;K,V&gt;</code> and
         * <code>List&lt;Map&lt;K,V&gt;&gt;</code> respectively.
         *
         * @param dataTable  the table to convert
         * @param type       the type to convert to
         * @param transposed whether the table should be transposed first.
         * @return an object of type
         */
        <T> T convert(DataTable dataTable, Type type, boolean transposed);

        /**
         * Converts a {@link DataTable} to a list.
         * <p>
         * A table converter may either map each row or each individual cell to a list element.
         * <p>
         * For example:
         * <p>
         * <pre>
         * | Annie M. G. Schmidt | 1911-03-20 |
         * | Roald Dahl          | 1916-09-13 |
         *
         * convert.toList(table, String.class);
         * </pre>
         * can become
         * <pre>
         *  [ "Annie M. G. Schmidt", "1911-03-20", "Roald Dahl", "1916-09-13" ]
         * </pre>
         * <p>
         * While:
         * <pre>
         *   convert.toList(table, Author.class);
         * </pre>
         * <p>
         * can become:
         * <p>
         * <pre>
         * [
         *   Author[ name: Annie M. G. Schmidt, birthDate: 1911-03-20 ],
         *   Author[ name: Roald Dahl,          birthDate: 1916-09-13 ]
         * ]
         * </pre>
         * <p>
         * Likewise:
         * <p>
         * <pre>
         *  | firstName   | lastName | birthDate  |
         *  | Annie M. G. | Schmidt  | 1911-03-20 |
         *  | Roald       | Dahl     | 1916-09-13 |
         *
         * convert.toList(table, Authors.class);
         * </pre>
         * can become:
         * <pre>
         *  [
         *   Author[ firstName: Annie M. G., lastName: Schmidt,  birthDate: 1911-03-20 ],
         *   Author[ firstName: Roald,       lastName: Dahl,     birthDate: 1916-09-13 ]
         *  ]
         * </pre>
         *
         * @param dataTable the table to convert
         * @param itemType  the  list item type to convert to
         * @return a list of objects of <code>itemType</code>
         */
        <T> List<T> toList(DataTable dataTable, Type itemType);

        /**
         * Converts a {@link DataTable} to a list of lists.
         * <p>
         * Each row maps to a list, each table cell a list entry.
         * <p>
         * For example:
         * <p>
         * <pre>
         * | Annie M. G. Schmidt | 1911-03-20 |
         * | Roald Dahl          | 1916-09-13 |
         *
         * convert.toLists(table, String.class);
         * </pre>
         * can become
         * <pre>
         *  [
         *    [ "Annie M. G. Schmidt", "1911-03-20" ],
         *    [ "Roald Dahl",          "1916-09-13" ]
         *  ]
         * </pre>
         * <p>
         *
         * @param dataTable the table to convert
         * @param itemType  the  list item type to convert to
         * @return a list of lists of objects of <code>itemType</code>
         */
        <T> List<List<T>> toLists(DataTable dataTable, Type itemType);

        /**
         * Converts a {@link DataTable} to a map.
         * <p>
         * The left column of the table is used to instantiate the key values. The other columns are used to instantiate
         * the values.
         * <p>
         * For example:
         * <p>
         * <pre>
         * | 4a1 | Annie M. G. Schmidt | 1911-03-20 |
         * | c92 | Roald Dahl          | 1916-09-13 |
         *
         * convert.toMap(table, Id.class, Authors.class);
         * </pre>
         * can become:
         * <pre>
         *  {
         *   Id[ 4a1 ]: Author[ name: Annie M. G. Schmidt, birthDate: 1911-03-20 ],
         *   Id[ c92 ]: Author[ name: Roald Dahl,          birthDate: 1916-09-13 ]
         *  }
         * </pre>
         * <p>
         * The header cells may be used to map values into the types. When doing so the first header cell may be
         * left blank.
         * <p>
         * For example:
         * <p>
         * <pre>
         * |     | firstName   | lastName | birthDate  |
         * | 4a1 | Annie M. G. | Schmidt  | 1911-03-20 |
         * | c92 | Roald       | Dahl     | 1916-09-13 |
         *
         * convert.toMap(table, Id.class, Authors.class);
         * </pre>
         * can becomes:
         * <pre>
         *  {
         *   Id[ 4a1 ]: Author[ firstName: Annie M. G., lastName: Schmidt, birthDate: 1911-03-20 ],
         *   Id[ c92 ]: Author[ firstName: Roald,       lastName: Dahl,    birthDate: 1916-09-13 ]
         *  }
         * </pre>
         *
         * @param dataTable the table to convert
         * @param keyType   the  key type to convert to
         * @param valueType the  value to convert to
         * @return a map of <code>keyType</code> <code>valueType</code>
         */

        <K, V> Map<K, V> toMap(DataTable dataTable, Type keyType, Type valueType);

        /**
         * Converts a {@link DataTable} to a list of maps.
         * <p>
         * Each map represents a row in the table. The map keys are the column headers.
         * <p>
         * For example:
         * <p>
         * <pre>
         * | firstName   | lastName | birthDate  |
         * | Annie M. G. | Schmidt  | 1911-03-20 |
         * | Roald       | Dahl     | 1916-09-13 |
         * </pre>
         * can become:
         * <pre>
         *  [
         *   {firstName: Annie M. G., lastName: Schmidt, birthDate: 1911-03-20 }
         *   {firstName: Roald,       lastName: Dahl,    birthDate: 1916-09-13 }
         *  ]
         * </pre>
         *
         * @param dataTable the table to convert
         * @param keyType   the  key type to convert to
         * @param valueType the  value to convert to
         * @return a list of maps of <code>keyType</code> <code>valueType</code>
         */
        <K, V> List<Map<K, V>> toMaps(DataTable dataTable, Type keyType, Type valueType);

    }

}