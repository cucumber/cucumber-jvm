package cucumber.runtime;

import cucumber.api.TypeRegistry;
import io.cucumber.cucumberexpressions.CucumberExpressionException;
import cucumber.api.datatable.DataTableType;
import io.cucumber.cucumberexpressions.ParameterType;
import cucumber.api.datatable.DataTable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static cucumber.api.datatable.DataTableType.aListOf;
import static io.cucumber.cucumberexpressions.TypeUtils.listItemType;
import static io.cucumber.cucumberexpressions.TypeUtils.mapKeyType;
import static io.cucumber.cucumberexpressions.TypeUtils.mapValueType;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;

public final class TypeRegistryTableConverter implements cucumber.api.datatable.TableConverter {

    private final TypeRegistry registry;

    TypeRegistryTableConverter(TypeRegistry registry) {
        this.registry = registry;
    }

    @Override
    public <T> T convert(DataTable dataTable, Type type, boolean transposed) {
        if (dataTable == null) throw new CucumberExpressionException("dataTable may not be null");
        if (type == null) throw new CucumberExpressionException("type may not be null");

        if (transposed) {
            dataTable = dataTable.transpose();
        }

        DataTableType tableType = registry.lookupTableTypeByType(type);
        if (tableType != null) {
            return (T) tableType.transform(dataTable.cells());
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
        if (dataTable == null) throw new CucumberExpressionException("dataTable may not be null");
        if (itemType == null) throw new CucumberExpressionException("itemType may not be null");


        DataTableType tableType = registry.lookupTableTypeByType(aListOf(itemType));
        if (tableType != null) {
            return unmodifiableList((List<T>) tableType.transform(dataTable.cells()));
        }

        ParameterType<T> parameterType = registry.lookupParameterTypeByType(itemType);
        if (parameterType != null) {
            return toList(dataTable, parameterType);
        }

        throw new CucumberExpressionException(String.format("Can't convert DataTable to List<%s>", itemType));
    }

    private <T> List<T> toList(DataTable dataTable, ParameterType<T> parameterType) {
        List<T> result = new ArrayList<T>();
        for (List<String> row : dataTable.cells()) {
            for (String cell : row) {
                result.add(parameterType.transform(singletonList(cell)));
            }
        }
        return unmodifiableList(result);
    }


    @Override
    public <T> List<List<T>> toLists(DataTable dataTable, Type itemType) {
        if (dataTable == null) throw new CucumberExpressionException("dataTable may not be null");
        if (itemType == null) throw new CucumberExpressionException("itemType may not be null");

        ParameterType<T> parameterType = registry.lookupParameterTypeByType(itemType);
        if (parameterType != null) {
            return toLists(dataTable, parameterType);
        }

        throw new CucumberExpressionException(String.format("Can't convert DataTable to List<List<%s>>", itemType));
    }

    private <T> List<List<T>> toLists(DataTable dataTable, ParameterType<T> parameterType) {
        List<List<T>> result = new ArrayList<List<T>>();
        for (List<String> row : dataTable.cells()) {
            List<T> convertedRow = new ArrayList<T>();
            for (String cell : row) {
                convertedRow.add(parameterType.transform(singletonList(cell)));
            }
            result.add(unmodifiableList(convertedRow));
        }
        return unmodifiableList(result);
    }

    @Override
    public <K, V> Map<K, V> toMap(DataTable dataTable, Type keyType, Type valueType) {
        if (dataTable == null) throw new CucumberExpressionException("dataTable may not be null");
        if (keyType == null) throw new CucumberExpressionException("keyType may not be null");
        if (valueType == null) throw new CucumberExpressionException("valueType may not be null");

        ParameterType<K> keyConverter = registry.lookupParameterTypeByType(keyType);
        ParameterType<V> valueConverter = registry.lookupParameterTypeByType(valueType);

        if (keyConverter == null || valueConverter == null) {
            throw new CucumberExpressionException(String.format("Can't convert DataTable to Map<%s,%s>", keyType, valueType));
        }

        Map<K, V> result = new LinkedHashMap<K,V>();
        for (List<String> row : dataTable.cells()) {
            if (row.size() != 2) {
                //TODO: Use table converter instead to map remaining row to object
                throw new CucumberExpressionException("A DataTable can only be converted to a Map when there are 2 columns"); //TODO: LIES!
            }
            K key = keyConverter.transform(singletonList(row.get(0)));
            V value = valueConverter.transform(singletonList(row.get(1)));
            result.put(key, value);
        }
        return Collections.unmodifiableMap(result);
    }

    @Override
    public <K, V> List<Map<K, V>> toMaps(DataTable dataTable, Type keyType, Type valueType) {
        if (dataTable == null) throw new CucumberExpressionException("dataTable may not be null");
        if (keyType == null) throw new CucumberExpressionException("keyType may not be null");
        if (valueType == null) throw new CucumberExpressionException("valueType may not be null");

        ParameterType<K> keyConverter = registry.lookupParameterTypeByType(keyType);
        ParameterType<V> valueConverter = registry.lookupParameterTypeByType(valueType);


        if (keyConverter == null || valueConverter == null) {
            throw new CucumberExpressionException(String.format("Can't convert DataTable to List<Map<%s,%s>>", keyType, valueType));
        }

        List<String> keyStrings = dataTable.topCells();
        if (keyStrings.isEmpty()) {
            return emptyList();
        }

        List<Map<K, V>> result = new ArrayList<Map<K,V>>();
        List<K> keys = new ArrayList<K>();
        for (String keyString : keyStrings) {
            keys.add(keyConverter.transform(singletonList(keyString)));
        }
        List<List<String>> valueRows = dataTable.rows(1);
        for (List<String> valueRow : valueRows) {
            Map<K, V> map = new LinkedHashMap<K,V>();
            int i = 0;
            for (String cell : valueRow) {
                map.put(keys.get(i), valueConverter.transform(singletonList(cell)));
                i++;
            }
            result.add(Collections.unmodifiableMap(map));
        }
        return unmodifiableList(result);
    }

    @Override
    public DataTable toTable(List<?> objects, String... columnNames) {
        throw new UnsupportedOperationException("TODO");
    }

}
