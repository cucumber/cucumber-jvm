package cucumber.api.datatable;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public interface TableConverter {
    /**
     * This method converts a {@link DataTable} to another type.
     *
     * @param dataTable  the table to convert
     * @param type       the type to convert to
     * @param transposed whether the table should be transposed first.
     * @return the transformed object.
     */
    <T> T convert(DataTable dataTable, Type type, boolean transposed);

    <T> List<T> toList(DataTable dataTable, Type itemType);

    <T> List<List<T>> toLists(DataTable dataTable, Type itemType);

    <K, V> Map<K, V> toMap(DataTable dataTable, Type keyType, Type valueType);

    <K, V> List<Map<K, V>> toMaps(DataTable dataTable, Type keyType, Type valueType);

    /**
     * Converts a List of objects to a DataTable.
     *
     * @param objects     the objects to convert
     * @param columnNames an explicit list of column names
     * @return a DataTable
     */
    DataTable toTable(List<?> objects, String... columnNames);
}
