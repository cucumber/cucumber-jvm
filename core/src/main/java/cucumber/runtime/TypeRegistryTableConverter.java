package cucumber.runtime;

import cucumber.api.TypeRegistry;
import io.cucumber.cucumberexpressions.CucumberExpressionException;
import cucumber.api.datatable.DataTableType;
import cucumber.api.datatable.DataTable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static cucumber.api.datatable.DataTableType.aListOf;
import static cucumber.runtime.Utils.listItemType;
import static cucumber.runtime.Utils.mapKeyType;
import static cucumber.runtime.Utils.mapValueType;
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

        throw new CucumberExpressionException(String.format("Can't convert DataTable to List<%s>", itemType));
    }


    @Override
    public <T> List<List<T>> toLists(DataTable dataTable, Type itemType) {
        if (dataTable == null) throw new CucumberExpressionException("dataTable may not be null");
        if (itemType == null) throw new CucumberExpressionException("itemType may not be null");

        DataTableType tableType = registry.lookupTableTypeByType(aListOf(aListOf(itemType)));
        if (tableType != null) {
            return unmodifiableList((List<List<T>>) tableType.transform(dataTable.cells()));
        }

        throw new CucumberExpressionException(String.format("Can't convert DataTable to List<List<%s>>", itemType));
    }

    @Override
    public <K, V> Map<K, V> toMap(DataTable dataTable, Type keyType, Type valueType) {
        if (dataTable == null) throw new CucumberExpressionException("dataTable may not be null");
        if (keyType == null) throw new CucumberExpressionException("keyType may not be null");
        if (valueType == null) throw new CucumberExpressionException("valueType may not be null");

        DataTableType keyConverter = registry.lookupTableTypeByType(aListOf(keyType));
        DataTableType valueConverter = registry.lookupTableTypeByType(aListOf(valueType));

        if (keyConverter == null) {
            throw new CucumberExpressionException(String.format(
                "Can't convert DataTable to Map<%s,%s>. " +
                    "Please register a converter from DataTable to List<%s>", keyType, valueType, keyType));
        }

        if (valueConverter == null) {
            throw new CucumberExpressionException(String.format(
                "Can't convert DataTable to Map<%s,%s>" +
                    "Please register a converter from DataTable to List<%s>", keyType, valueType, valueType));
        }

        if (dataTable.width() < 2) {
            throw new CucumberExpressionException("A DataTable can only be converted to a Map when there are at least 2 columns");
        }

        List<List<String>> keyColumn = dataTable.columns(0, 2);
        List<List<String>> valueColumn = dataTable.columns(2, dataTable.width());

        List<K> keys = (List<K>) keyConverter.transform(keyColumn);
        List<V> values = (List<V>) valueConverter.transform(valueColumn);

        Iterator<K> keyIterator = keys.iterator();
        Iterator<V> valueIterator = values.iterator();

        Map<K, V> result = new LinkedHashMap<K, V>();
        while (keyIterator.hasNext() && valueIterator.hasNext()) {
            result.put(keyIterator.next(), valueIterator.next());
        }

        return Collections.unmodifiableMap(result);
    }

    @Override
    public <K, V> List<Map<K, V>> toMaps(DataTable dataTable, Type keyType, Type valueType) {
        if (dataTable == null) throw new CucumberExpressionException("dataTable may not be null");
        if (keyType == null) throw new CucumberExpressionException("keyType may not be null");
        if (valueType == null) throw new CucumberExpressionException("valueType may not be null");

        DataTableType keyConverter = registry.lookupTableTypeByType(aListOf(keyType));
        DataTableType valueConverter = registry.lookupTableTypeByType(aListOf(valueType));

        if (keyConverter == null) {
            //TODO: Replace with CucumberDataTableException and dedupe
            throw new CucumberExpressionException(String.format(
                "Can't convert DataTable to Map<%s,%s>. " +
                    "Please register a converter from DataTable to List<%s>", keyType, valueType, keyType));
        }

        if (valueConverter == null) {
            throw new CucumberExpressionException(String.format(
                "Can't convert DataTable to Map<%s,%s>" +
                    "Please register a converter from DataTable to List<%s>", keyType, valueType, valueType));
        }
        
        List<String> keyStrings = dataTable.topCells();
        if (keyStrings.isEmpty()) {
            return emptyList();
        }

        List<Map<K, V>> result = new ArrayList<Map<K, V>>();
        List<K> keys = new ArrayList<K>();
        for (String keyString : keyStrings) {
            List<List<String>> raw = singletonList(singletonList(keyString));
            keys.add((K) keyConverter.transform(raw));
        }
        List<List<String>> valueRows = dataTable.rows(1);
        for (List<String> valueRow : valueRows) {
            Map<K, V> map = new LinkedHashMap<K, V>();
            int i = 0;
            for (String cell : valueRow) {
                List<List<String>> raw = singletonList(singletonList(cell));
                map.put(keys.get(i), (V) valueConverter.transform(raw));
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
