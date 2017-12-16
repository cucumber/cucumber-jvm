package cucumber.api.datatable;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import cucumber.runtime.CucumberException;
import io.cucumber.cucumberexpressions.CucumberExpressionException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.cucumber.cucumberexpressions.TypeUtils.listItemType;
import static io.cucumber.cucumberexpressions.TypeUtils.mapKeyType;
import static io.cucumber.cucumberexpressions.TypeUtils.mapValueType;
import static java.util.Collections.unmodifiableList;

public class JacksonTableConverter implements TableConverter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public <T> T convert(DataTable dataTable, Type type, boolean transposed) {
        if (dataTable == null) throw new CucumberExpressionException("dataTable may not be null");
        if (type == null) throw new CucumberExpressionException("type may not be null");

        if (transposed) {
            dataTable = dataTable.transpose();
        }

        if (type.equals(DataTable.class)) {
            return (T) dataTable;
        }

        Type mapKeyType = mapKeyType(type);
        if (mapKeyType != null) {
            Type mapValueType = mapValueType(type);
            return (T) toMap(dataTable, mapKeyType, mapValueType);
        }

        Type itemType = listItemType(type);
        if (itemType == null) {
            throw new CucumberExpressionException(String.format("Can't convert DataTable to %1$s", type));
        }

        Type mapKeyItemType = mapKeyType(itemType);
        if (mapKeyItemType != null) {
            Type mapValueType = mapValueType(type);
            return (T) toMaps(dataTable, mapKeyItemType, mapValueType);
        } else if (Map.class.equals(itemType)) {
            // Non-generic map
            return (T) toMaps(dataTable, String.class, String.class);
        }

        Type listItemType = listItemType(itemType);
        if (listItemType != null) {
            return (T) toLists(dataTable, listItemType);
        } else if (List.class.equals(itemType)) {
            // Non-generic list
            return (T) toLists(dataTable, String.class);
        }

        return (T) toList(dataTable, itemType);
    }

    @Override
    public <T> List<T> toList(DataTable dataTable, Type itemType) {
        Class<? extends T> toValueType = requireClass(itemType);
        List<T> result = new ArrayList<T>();

        for (Map<String, String> row : dataTable.asMaps()) {
            result.add(objectMapper.convertValue(row, toValueType));
        }

        return unmodifiableList(result);
    }

    private static <T> Class<? extends T> requireClass(Type itemType) {
        return (Class<? extends T>) itemType;
    }

    @Override
    public <T> List<List<T>> toLists(DataTable dataTable, Type itemType) {
        Class<? extends T> toValueType = requireClass(itemType);

        List<List<T>> result = new ArrayList<List<T>>();

        for (List<String> row : dataTable.asLists()) {
            List<T> newRow = new ArrayList<T>(row.size());
            for (String item : row) {
                newRow.add(objectMapper.convertValue(item, toValueType));
            }
            result.add(unmodifiableList(newRow));
        }

        return unmodifiableList(result);
    }

    @Override
    public <K, V> Map<K, V> toMap(DataTable dataTable, Type keyType, Type valueType) {
        Class<? extends K> toKeyType = requireClass(keyType);
        Class<? extends V> toValueType = requireClass(valueType);

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (List<String> row : dataTable.raw()) {
            if (row.size() != 2) {
                throw new CucumberException("A DataTable can only be converted to a Map when there are 2 columns");
            }
            K key = objectMapper.convertValue(row.get(0), toKeyType);
            V value = objectMapper.convertValue(row.get(1), toValueType);
            result.put(key, value);
        }
        return Collections.unmodifiableMap(result);
    }

    @Override
    public <K, V> List<Map<K, V>> toMaps(DataTable dataTable, Type keyType, Type valueType) {
        Class<? extends K> toKeyType = requireClass(keyType);
        Class<? extends V> toValueType = requireClass(valueType);

        List<Map<K, V>> result = new ArrayList<Map<K, V>>();
        List<String> keyStrings = dataTable.topCells();
        List<K> keys = new ArrayList<K>();
        for (String keyString : keyStrings) {
            keys.add(objectMapper.convertValue(keyString, toKeyType));
        }
        List<List<String>> valueRows = dataTable.rows(1);
        for (List<String> valueRow : valueRows) {
            Map<K, V> map = new LinkedHashMap<K, V>();
            int i = 0;
            for (String cell : valueRow) {
                map.put(keys.get(i), objectMapper.convertValue(cell, toValueType));
                i++;
            }
            result.add(Collections.unmodifiableMap(map));
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public DataTable toTable(List<?> objects, String... columnNames) {
        List<List<String>> raw = new ArrayList<List<String>>();

        for (Object object : objects) {
            Map<String, Object> map = objectMapper.convertValue(object, Map.class);

            if (raw.isEmpty()) {
                List<String> header = new ArrayList<String>();
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    header.add(entry.getKey());
                }
                raw.add(header);
            }

            List<String> row = new ArrayList<String>();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                row.add(objectMapper.convertValue(entry.getValue(), String.class));
            }

            raw.add(row);
        }

        return new DataTable(raw, this);
    }
}
