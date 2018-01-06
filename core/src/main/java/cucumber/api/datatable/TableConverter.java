package cucumber.api.datatable;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

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
     * Delegates <code>toList</code>, <code>toLists</code>, <code>toMap</code> and <code>toMaps</code>
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
     * | Eva de Roovere  | 1978-06-14 |
     * | Herman van Veen | 1945-03-14 |
     *
     * convert.toList(table, String.class);
     * </pre>
     * can become
     * <pre>
     *  [ "Eva de Roovere", "1978-06-14", "Herman van Veen", "1945-03-14" ]
     * </pre>
     * <p>
     * <p>
     * While:
     * <pre>
     *   convert.toList(table, Artist.class);
     * </pre>
     * <p>
     * can become:
     * <p>
     * <pre>
     * [
     *   Artist[ name: Eva de Roovere,  birthDate: 1978-06-14 ],
     *   Artist[ name: Herman van Veen, birthDate: 1945-03-14 ]
     * ]
     * </pre>
     * <p>
     * Like wise:
     * <p>
     * <pre>
     *  | firstName | lastName   | birthDate  |
     *  | Eva       | de Roovere | 1978-06-14 |
     *  | Herman    | van Veen   | 1945-03-14 |
     *
     * convert.toList(table, Artists.class);
     * </pre>
     * can become:
     * <pre>
     *  [
     *   Artist[ firstName: Eva, lastName: de Roovere,  birthDate: 1978-06-14 ],
     *   Artist[ firstName: Herman, lastName: van Veen, birthDate: 1945-03-14 ]
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
     * | 4a1 | Eva de Roovere  | 1978-06-14 |
     * | c92 | Herman van Veen | 1945-03-14 |
     *
     * convert.toMap(table, Id.class, Artists.class);
     * </pre>
     * can become:
     * <pre>
     *  {
     *   Id[ 4a1 ]: Artist[ name: Eva de Roovere,  birthDate: 1978-06-14 ],
     *   Id[ c92 ]: Artist[ name: Herman van Veen, birthDate: 1945-03-14 ]
     *  }
     * </pre>
     * <p>
     * The header cells may be used to map values into the types. When doing so the first header cell may be
     * left blank.
     * <p>
     * For example:
     * <p>
     * <pre>
     * |     | firstName | lastName   | birthDate  |
     * | 4a1 | Eva       | de Roovere | 1978-06-14 |
     * | c92 | Herman    | van Veen   | 1945-03-14 |
     *
     * convert.toMap(table, Id.class, Artists.class);
     * </pre>
     * can becomes:
     * <pre>
     *  {
     *   Id[ 4a1 ]: Artist[ firstName: Eva, lastName: de Roovere,  birthDate: 1978-06-14 ],
     *   Id[ c92 ]: Artist[ firstName: Herman, lastName: van Veen, birthDate: 1945-03-14 ]
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
     * | firstName | lastName   | birthDate  |
     * | Eva       | de Roovere | 1978-06-14 |
     * | Herman    | van Veen   | 1945-03-14 |
     * </pre>
     * can become:
     * <pre>
     *  [
     *   {firstName: Eva, lastName: de Roovere,  birthDate: 1978-06-14 }
     *   {firstName: Herman, lastName: van Veen, birthDate: 1945-03-14 }
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
