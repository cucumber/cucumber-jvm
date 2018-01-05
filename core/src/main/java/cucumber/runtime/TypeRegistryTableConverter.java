package cucumber.runtime;

import cucumber.api.TypeRegistry;
import io.cucumber.cucumberexpressions.CucumberExpressionException;
import cucumber.api.datatable.DataTableType;
import cucumber.api.datatable.DataTable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static cucumber.api.datatable.DataTableType.aListOf;
import static cucumber.runtime.Utils.listItemType;
import static cucumber.runtime.Utils.mapKeyType;
import static cucumber.runtime.Utils.mapValueType;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

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

        if (dataTable.isEmpty()) {
            return emptyList();
        }

        DataTableType tableType = registry.lookupTableTypeByType(aListOf(itemType));
        if (tableType != null) {
            return unmodifiableList((List<T>) tableType.transform(dataTable.cells()));
        }


        DataTableType tableCellType = registry.lookupTableTypeByType(aListOf(aListOf(itemType)));
        if (tableCellType != null) {
            List<List<T>> cells = (List<List<T>>) tableCellType.transform(dataTable.cells());
            return unmodifiableList(unpack(cells));
        }

        if (dataTable.width() > 1) {
            throw new CucumberExpressionException(String.format(
                "Can't convert DataTable to List<%s>. " +
                    "Please register a DataTableType with a TableEntryTransformer, TableRowTransformer or TableCellTransformer for %s", itemType, itemType));
        }

        throw new CucumberExpressionException(String.format(
            "Can't convert DataTable to List<%s>. " +
                "Please register a DataTableType with a TableCellTransformer for %s", itemType, itemType));
    }

    private <T> List<T> unpack(List<List<T>> cells) {
        List<T> unpacked = new ArrayList<T>(cells.size());
        for (List<T> row : cells) {
            unpacked.addAll(row);
        }
        return unpacked;
    }


    @Override
    public <T> List<List<T>> toLists(DataTable dataTable, Type itemType) {
        if (dataTable == null) throw new CucumberExpressionException("dataTable may not be null");
        if (itemType == null) throw new CucumberExpressionException("itemType may not be null");

        if (dataTable.isEmpty()) {
            return emptyList();
        }

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

        if (dataTable.isEmpty()) {
            return emptyMap();
        }

        if (dataTable.width() < 2) {
            throw new CucumberExpressionException("A DataTable can only be converted to a Map when there are at least 2 columns");
        }

        List<List<String>> keyColumn = dataTable.columns(0, 1);
        List<List<String>> valueColumns = dataTable.columns(1, dataTable.width());

        if (dataTable.width() == 2) {
            return toMapOfCells(keyType, keyColumn, valueType, valueColumns);
        } else {
            return toMapOfEntries(keyType, keyColumn, valueType, valueColumns);
        }

    }

    private <K, V> Map<K, V> toMapOfCells(Type keyType, List<List<String>> keyColumn, Type valueType, List<List<String>> valueColumns) {
        DataTableType keyConverter = registry.lookupTableTypeByType(aListOf(aListOf(keyType)));
        if (keyConverter == null) {
            throw new CucumberExpressionException(String.format(
                "Can't convert DataTable to Map<%s,%s>. " +
                    "Please register a DataTableType with a TableCellTransformer for %s", keyType, valueType, keyType));
        }

        DataTableType valueConverter = registry.lookupTableTypeByType(aListOf(aListOf(valueType)));
        if (valueConverter == null) {
            throw new CucumberExpressionException(String.format(
                "Can't convert DataTable to Map<%s,%s>" +
                    "Please register a DataTableType with a TableCellTransformer for %s", keyType, valueType, valueType));
        }

        List<List<K>> keys = (List<List<K>>) keyConverter.transform(keyColumn);
        List<List<V>> values = (List<List<V>>) valueConverter.transform(valueColumns);

        Iterator<List<K>> keyIterator = keys.iterator();
        Iterator<List<V>> valueIterator = values.iterator();

        Map<K, V> result = new LinkedHashMap<K, V>();
        while (keyIterator.hasNext() && valueIterator.hasNext()) {
            result.put(keyIterator.next().get(0), valueIterator.next().get(0));
        }

        return unmodifiableMap(result);
    }

    private <K, V> Map<K, V> toMapOfEntries(Type keyType, List<List<String>> keyColumn, Type valueType, List<List<String>> valueColumns) {
        DataTableType keyConverter = registry.lookupTableTypeByType(aListOf(keyType));
        if (keyConverter == null) {
            throw new CucumberExpressionException(String.format(
                "Can't convert DataTable to Map<%s,%s>. " +
                    "Please register a DataTableType with a TableEntryTransformer for %s", keyType, valueType, keyType));
        }

        DataTableType valueConverter = registry.lookupTableTypeByType(aListOf(valueType));
        if (valueConverter == null) {
            throw new CucumberExpressionException(String.format(
                "Can't convert DataTable to Map<%s,%s>" +
                    "Please register a DataTableType with a TableEntryTransformer for %s", keyType, valueType, valueType));
        }

        List<K> keys = (List<K>) keyConverter.transform(keyColumn);
        List<V> values = (List<V>) valueConverter.transform(valueColumns);

        Iterator<K> keyIterator = keys.iterator();
        Iterator<V> valueIterator = values.iterator();

        Map<K, V> result = new LinkedHashMap<K, V>();
        while (keyIterator.hasNext() && valueIterator.hasNext()) {
            result.put(keyIterator.next(), valueIterator.next());
        }

        return unmodifiableMap(result);
    }

    @Override
    public <K, V> List<Map<K, V>> toMaps(DataTable dataTable, Type keyType, Type valueType) {
        if (dataTable == null) throw new CucumberExpressionException("dataTable may not be null");
        if (keyType == null) throw new CucumberExpressionException("keyType may not be null");
        if (valueType == null) throw new CucumberExpressionException("valueType may not be null");

        if (dataTable.isEmpty()) {
            return emptyList();
        }

        DataTableType keyConverter = registry.lookupTableTypeByType(aListOf(aListOf(keyType)));
        DataTableType valueConverter = registry.lookupTableTypeByType(aListOf(aListOf(valueType)));

        if (keyConverter == null) {
            //TODO: Replace with CucumberDataTableException and dedupe
            throw new CucumberExpressionException(String.format(
                "Can't convert DataTable to Map<%s,%s>. " +
                    "Please register a DataTableType with a TableCellTransformer for %s",
                keyType, valueType, keyType));
        }

        if (valueConverter == null) {
            throw new CucumberExpressionException(String.format(
                "Can't convert DataTable to Map<%s,%s>. " +
                    "Please register a DataTableType with a TableCellTransformer for %s",
                keyType, valueType, valueType));
        }

        List<List<String>> keyStrings = dataTable.rows(0, 1);
        if (keyStrings.isEmpty() || keyStrings.get(0).isEmpty()) {
            return emptyList();
        }

        List<Map<K, V>> result = new ArrayList<Map<K, V>>();
        List<List<K>> keys = (List<List<K>>) keyConverter.transform(keyStrings);

        List<List<String>> valueRows = dataTable.rows(1);
        List<List<V>> transform = (List<List<V>>) valueConverter.transform(valueRows);

        for (List<V> valueRow : transform) {
            Map<K, V> map = new LinkedHashMap<K, V>();
            int i = 0;
            for (V cell : valueRow) {
                map.put(keys.get(0).get(i), cell);
                i++;
            }
            result.add(unmodifiableMap(map));
        }
        return unmodifiableList(result);
    }

}
