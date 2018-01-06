package cucumber.api.datatable;

import cucumber.api.datatable.DataTable.TableConverter;

import io.cucumber.cucumberexpressions.CucumberExpressionException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static cucumber.api.datatable.TypeFactory.aListOf;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.nCopies;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

public final class DataTableTypeRegistryTableConverter implements TableConverter {

    private final DataTableTypeRegistry registry;

    public DataTableTypeRegistryTableConverter(DataTableTypeRegistry registry) {
        this.registry = registry;
    }


    @Override
    public <T> T convert(DataTable dataTable, Type type, boolean transposed) {
        if (dataTable == null) throw new NullPointerException("dataTable may not be null");
        if (type == null) throw new NullPointerException("type may not be null");

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
            Type mapValueType = mapValueType(itemType);
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
        if (dataTable == null) throw new NullPointerException("dataTable may not be null");
        if (itemType == null) throw new NullPointerException("itemType may not be null");

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
                    "Please register a DataTableType with a TableEntryTransformer or TableRowTransformer for %s", itemType, itemType));
        }

        throw new CucumberExpressionException(String.format(
            "Can't convert DataTable to List<%s>. " +
                "Please register a DataTableType with a TableEntryTransformer, TableRowTransformer or TableCellTransformer for %s", itemType, itemType));
    }

    @Override
    public <T> List<List<T>> toLists(DataTable dataTable, Type itemType) {
        if (dataTable == null) throw new NullPointerException("dataTable may not be null");
        if (itemType == null) throw new NullPointerException("itemType may not be null");

        if (dataTable.isEmpty()) {
            return emptyList();
        }

        DataTableType tableType = registry.lookupTableTypeByType(aListOf(aListOf(itemType)));
        if (tableType != null) {
            return unmodifiableList((List<List<T>>) tableType.transform(dataTable.cells()));
        }

        throw new NullPointerException(String.format("Can't convert DataTable to List<List<%s>>", itemType));
    }

    @Override
    public <K, V> Map<K, V> toMap(DataTable dataTable, Type keyType, Type valueType) {
        if (dataTable == null) throw new NullPointerException("dataTable may not be null");
        if (keyType == null) throw new NullPointerException("keyType may not be null");
        if (valueType == null) throw new NullPointerException("valueType may not be null");

        if (dataTable.isEmpty()) {
            return emptyMap();
        }
        List<List<String>> keyColumn = dataTable.columns(0, 1);
        List<List<String>> valueColumns = dataTable.columns(1);

        String firstHeaderCell = keyColumn.get(0).get(0);
        boolean firstHeaderCellIsBlank = firstHeaderCell == null || firstHeaderCell.isEmpty();
        List<K> keys = convertEntryKeys(keyType, keyColumn, valueType, firstHeaderCellIsBlank);

        if (valueColumns.get(0).isEmpty()) {
            return createMap(keyType, keys, valueType, nCopies(keys.size(), (V) null));
        }

        boolean keysImplyTableRowTransformer = keys.size() == dataTable.height() - 1;
        List<V> values = convertEntryValues(keyType, valueType, valueColumns, keysImplyTableRowTransformer);

        if (keys.size() != values.size()) {
            throw createKeyValueMismatchException(firstHeaderCellIsBlank, keys.size(), keyType, values.size(), valueType);
        }

        return createMap(keyType, keys, valueType, values);
    }

    private static <K, V> Map<K, V> createMap(Type keyType, List<K> keys, Type valueType, List<V> values) {
        Iterator<K> keyIterator = keys.iterator();
        Iterator<V> valueIterator = values.iterator();
        Map<K, V> result = new LinkedHashMap<K, V>();
        while (keyIterator.hasNext() && valueIterator.hasNext()) {
            K key = keyIterator.next();
            V value = valueIterator.next();
            V replaced = result.put(key, value);
            if (replaced != null) {
                throw new CucumberDataTableException(String.format("Can't convert DataTable to Map<%s,%s>. " +
                        "Encountered duplicate key %s with values %s and %s",
                    keyType, valueType, key, replaced, value));
            }
        }

        return unmodifiableMap(result);
    }

    private <K> List<K> convertEntryKeys(Type keyType, List<List<String>> keyColumn, Type valueType, boolean firstHeaderCellIsBlank) {
        if (firstHeaderCellIsBlank) {
            DataTableType keyConverter;
            keyConverter = registry.lookupTableTypeByType(aListOf(aListOf(keyType)));
            if (keyConverter == null) {
                throw new CucumberDataTableException(String.format(
                    "Can't convert DataTable to Map<%s,%s>. " +
                        "Please register a DataTableType with a TableCellTransformer for %s", keyType, valueType, keyType));
            }
            return unpack((List<List<K>>) keyConverter.transform(keyColumn.subList(1, keyColumn.size())));
        }

        DataTableType entryKeyConverter = registry.lookupTableTypeByType(aListOf(keyType));
        if (entryKeyConverter != null) {
            return (List<K>) entryKeyConverter.transform(keyColumn);
        }

        DataTableType cellKeyConverter = registry.lookupTableTypeByType(aListOf(aListOf(keyType)));
        if (cellKeyConverter != null) {
            return unpack((List<List<K>>) cellKeyConverter.transform(keyColumn));
        }

        throw new CucumberDataTableException(String.format(
            "Can't convert DataTable to Map<%s,%s>. " +
                "Please register a DataTableType with a TableEntryTransformer or TableCellTransformer for %s", keyType, valueType, keyType));
    }

    private <V> List<V> convertEntryValues(Type keyType, Type valueType, List<List<String>> valueColumns, boolean keysImplyTableEntryTransformer) {
        DataTableType entryValueConverter = registry.lookupTableTypeByType(aListOf(valueType));
        if (entryValueConverter != null) {
            return (List<V>) entryValueConverter.transform(valueColumns);
        }

        if (keysImplyTableEntryTransformer) {
            throw new CucumberDataTableException(String.format(
                "Can't convert DataTable to Map<%s,%s>" +
                    "Please register a DataTableType with a TableEntryTransformer for %s", keyType, valueType, valueType));
        }

        DataTableType cellValueConverter = registry.lookupTableTypeByType(aListOf(aListOf(valueType)));
        if (cellValueConverter != null) {
            return unpack((List<List<V>>) cellValueConverter.transform(valueColumns));
        }

        throw new CucumberDataTableException(String.format(
            "Can't convert DataTable to Map<%s,%s>" +
                "Please register a DataTableType with a TableEntryTransformer or TableCellTransformer for %s", keyType, valueType, valueType));
    }

    @Override
    public <K, V> List<Map<K, V>> toMaps(DataTable dataTable, Type keyType, Type valueType) {
        if (dataTable == null) throw new NullPointerException("dataTable may not be null");
        if (keyType == null) throw new NullPointerException("keyType may not be null");
        if (valueType == null) throw new NullPointerException("valueType may not be null");

        if (dataTable.isEmpty()) {
            return emptyList();
        }

        DataTableType keyConverter = registry.lookupTableTypeByType(aListOf(aListOf(keyType)));
        DataTableType valueConverter = registry.lookupTableTypeByType(aListOf(aListOf(valueType)));

        if (keyConverter == null) {
            //TODO: Replace with CucumberDataTableException and dedupe
            throw new CucumberDataTableException(String.format(
                "Can't convert DataTable to Map<%s,%s>. " +
                    "Please register a DataTableType with a TableCellTransformer for %s",
                keyType, valueType, keyType));
        }

        if (valueConverter == null) {
            throw new CucumberDataTableException(String.format(
                "Can't convert DataTable to Map<%s,%s>. " +
                    "Please register a DataTableType with a TableCellTransformer for %s",
                keyType, valueType, valueType));
        }

        List<List<String>> keyStrings = dataTable.rows(0, 1);

        List<Map<K, V>> result = new ArrayList<Map<K, V>>();
        List<K> keys = unpack((List<List<K>>) keyConverter.transform(keyStrings));

        List<List<String>> valueRows = dataTable.rows(1);

        if (valueRows.isEmpty()) {
            return emptyList();
        }

        List<List<V>> transform = (List<List<V>>) valueConverter.transform(valueRows);

        for (List<V> valueRow : transform) {
            result.add(createMap(keyType, keys, valueType, valueRow));
        }
        return unmodifiableList(result);
    }

    private static <T> List<T> unpack(List<List<T>> cells) {
        List<T> unpacked = new ArrayList<T>(cells.size());
        for (List<T> row : cells) {
            unpacked.addAll(row);
        }
        return unpacked;
    }

    private static CucumberDataTableException createKeyValueMismatchException(boolean firstHeaderCellIsBlank, int keySize, Type keyType, int valueSize, Type valueType) {
        if (firstHeaderCellIsBlank) {
            return new CucumberDataTableException(String.format(
                "Can't convert DataTable to Map<%s,%s>. " +
                    "There are more values then keys. The first header cell was left blank. You can add a value there",
                keyType, valueType));
        }

        if (keySize > valueSize) {
            return new CucumberDataTableException(String.format(
                "Can't convert DataTable to Map<%s,%s>. " +
                    "There are more keys then values. " +
                    "Did you use a TableEntryTransformer for the value while using a TableRow or TableCellTransformer for the keys?",
                keyType, valueType));
        }

        if (valueSize % keySize == 0) {
            return new CucumberDataTableException(String.format(
                "Can't convert DataTable to Map<%s,%s>. " +
                    "There is more then one values per key. " +
                    "Did you mean to transform to Map<%s,List<%s>> instead?",
                keyType, valueType, keyType, valueType));
        }

        return new CucumberDataTableException(String.format(
            "Can't convert DataTable to Map<%s,%s>. " +
                "There are more values then keys. " +
                "Did you use a TableEntryTransformer for the key while using a TableRow or TableCellTransformer for the value?",
            keyType, valueType));

    }

    private static Type listItemType(Type type) {
        return typeArg(type, List.class, 0);
    }

    private static Type mapKeyType(Type type) {
        return typeArg(type, Map.class, 0);
    }

    private static Type mapValueType(Type type) {
        return typeArg(type, Map.class, 1);
    }

    private static Type typeArg(Type type, Class<?> wantedRawType, int index) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type rawType = parameterizedType.getRawType();
            if (rawType instanceof Class && wantedRawType.isAssignableFrom((Class) rawType)) {
                Type result = parameterizedType.getActualTypeArguments()[index];
                if (result instanceof TypeVariable) {
                    throw new CucumberDataTableException("Generic types must be explicit");
                }
                return result;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
