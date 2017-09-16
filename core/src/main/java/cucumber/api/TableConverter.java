package cucumber.api;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public interface TableConverter {
    <T> T convert(DataTable dataTable, Type type, boolean transposed);

    <T> List<T> toList(DataTable dataTable, Type itemType);

    <T> List<List<T>> toLists(DataTable dataTable, Type itemType);

    <K, V> Map<K, V> toMap(DataTable dataTable, Type keyType, Type valueType);

    <K, V> List<Map<K, V>> toMaps(DataTable dataTable, Type keyType, Type valueType);

    DataTable toTable(List<?> objects, String... columnNames);
}
