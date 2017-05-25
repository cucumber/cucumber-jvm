package cucumber.runtime.table;

import cucumber.api.DataTable;
import cucumber.deps.com.thoughtworks.xstream.converters.ConversionException;
import cucumber.deps.com.thoughtworks.xstream.converters.SingleValueConverter;
import cucumber.deps.com.thoughtworks.xstream.converters.reflection.AbstractReflectionConverter;
import cucumber.deps.com.thoughtworks.xstream.io.HierarchicalStreamReader;
import cucumber.runtime.CucumberException;
import cucumber.runtime.ParameterInfo;
import cucumber.runtime.xstream.CellWriter;
import cucumber.runtime.xstream.ComplexTypeWriter;
import cucumber.runtime.xstream.ListOfComplexTypeReader;
import cucumber.runtime.xstream.ListOfSingleValueWriter;
import cucumber.runtime.xstream.LocalizedXStreams;
import cucumber.runtime.xstream.MapWriter;
import cucumber.util.Mapper;
import gherkin.pickles.PickleCell;
import gherkin.pickles.PickleRow;
import gherkin.pickles.PickleTable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static cucumber.runtime.Utils.listItemType;
import static cucumber.runtime.Utils.mapKeyType;
import static cucumber.runtime.Utils.mapValueType;
import static cucumber.util.FixJava.map;
import static java.util.Arrays.asList;

/**
 * This class converts a {@link cucumber.api.DataTable} to various other types.
 */
public class TableConverter {
    private final LocalizedXStreams.LocalizedXStream xStream;
    private final ParameterInfo parameterInfo;

    public TableConverter(LocalizedXStreams.LocalizedXStream xStream, ParameterInfo parameterInfo) {
        this.xStream = xStream;
        this.parameterInfo = parameterInfo;
    }

    /**
     * This method converts a {@link cucumber.api.DataTable} to abother type.
     * When a Step Definition is passed a Gherkin Data Table, the runtime will use this method to convert the
     * {@link cucumber.api.DataTable} to the declared type before invoking the Step Definition.
     * <p/>
     * This method uses reflection to inspect the type and delegates to the appropriate {@code toXxx} method.
     *
     * @param dataTable  the table to convert
     * @param type       the type to convert to
     * @param transposed whether the table should be transposed first.
     * @return the transformed object.
     */
    public <T> T convert(DataTable dataTable, Type type, boolean transposed) {
        if (transposed) {
            dataTable = dataTable.transpose();
        }

        if (type == null || (type instanceof Class && ((Class) type).isAssignableFrom(DataTable.class))) {
            return (T) dataTable;
        }

        Type mapKeyType = mapKeyType(type);
        if (mapKeyType != null) {
            Type mapValueType = mapValueType(type);
            return (T) toMap(dataTable, mapKeyType, mapValueType);
        }

        Type itemType = listItemType(type);
        if (itemType == null) {
            throw new CucumberException("Not a Map or List type: " + type);
        }

        Type listItemType = listItemType(itemType);
        if (listItemType != null) {
            return (T) toLists(dataTable, listItemType);
        } else {
            SingleValueConverter singleValueConverter = xStream.getSingleValueConverter(itemType);
            if (singleValueConverter != null) {
                return (T) toList(dataTable, singleValueConverter);
            } else {
                if (itemType instanceof Class) {
                    if (Map.class.equals(itemType)) {
                        // Non-generic map
                        return (T) toMaps(dataTable, String.class, String.class);
                    } else {
                        return (T) toListOfComplexType(dataTable, (Class) itemType);
                    }
                } else {
                    return (T) toMaps(dataTable, mapKeyType(itemType), mapValueType(itemType));
                }
            }
        }
    }

    private <T> List<T> toListOfComplexType(DataTable dataTable, Class<T> itemType) {
        HierarchicalStreamReader reader = new ListOfComplexTypeReader(itemType, convertTopCellsToFieldNames(dataTable), dataTable.cells(1));
        try {
            xStream.setParameterInfo(parameterInfo);
            return Collections.unmodifiableList((List<T>) xStream.unmarshal(reader));
        } catch (AbstractReflectionConverter.UnknownFieldException e) {
            throw new CucumberException(e.getShortMessage());
        } catch (AbstractReflectionConverter.DuplicateFieldException e) {
            throw new CucumberException(e.getShortMessage());
        } catch (ConversionException e) {
            if (e.getCause() instanceof NullPointerException) {
                throw new CucumberException(String.format("Can't assign null value to one of the primitive fields in %s. Please use boxed types.", e.get("class")));
            } else {
                throw new CucumberException(e);
            }
        } finally {
            xStream.unsetParameterInfo();
        }
    }

    public <T> List<T> toList(DataTable dataTable, Type itemType) {
        SingleValueConverter itemConverter = xStream.getSingleValueConverter(itemType);
        if (itemConverter != null) {
            return toList(dataTable, itemConverter);
        } else {
            if (itemType instanceof Class) {
                return toListOfComplexType(dataTable, (Class<T>) itemType);
            } else {
                throw new CucumberException(String.format("Can't convert DataTable to List<%s>", itemType));
            }
        }
    }

    private <T> List<T> toList(DataTable dataTable, SingleValueConverter itemConverter) {
        List<T> result = new ArrayList<T>();

        for (List<String> row : dataTable.raw()) {
            for (String cell : row) {
                result.add((T) itemConverter.fromString(cell));
            }
        }
        return Collections.unmodifiableList(result);
    }

    public <T> List<List<T>> toLists(DataTable dataTable, Type itemType) {
        try {
            xStream.setParameterInfo(parameterInfo);
            SingleValueConverter itemConverter = xStream.getSingleValueConverter(itemType);
            if (itemConverter == null) {
                throw new CucumberException(String.format("Can't convert DataTable to List<List<%s>>", itemType));
            }

            List<List<T>> result = new ArrayList<List<T>>();
            for (List<String> row : dataTable.raw()) {
                List<T> convertedRow = new ArrayList<T>();
                for (String cell : row) {
                    convertedRow.add((T) itemConverter.fromString(cell));
                }
                result.add(Collections.unmodifiableList(convertedRow));
            }
            return Collections.unmodifiableList(result);
        } finally {
            xStream.unsetParameterInfo();
        }
    }

    public <K, V> Map<K, V> toMap(DataTable dataTable, Type keyType, Type valueType) {
        try {
            xStream.setParameterInfo(parameterInfo);
            SingleValueConverter keyConverter = xStream.getSingleValueConverter(keyType);
            SingleValueConverter valueConverter = xStream.getSingleValueConverter(valueType);

            if (keyConverter == null || valueConverter == null) {
                throw new CucumberException(String.format("Can't convert DataTable to Map<%s,%s>", keyType, valueType));
            }

            Map<K, V> result = new LinkedHashMap<K, V>();
            for (List<String> row : dataTable.raw()) {
                if (row.size() != 2) {
                    throw new CucumberException("A DataTable can only be converted to a Map when there are 2 columns");
                }
                K key = (K) keyConverter.fromString(row.get(0));
                V value = (V) valueConverter.fromString(row.get(1));
                result.put(key, value);
            }
            return Collections.unmodifiableMap(result);
        } finally {
            xStream.unsetParameterInfo();
        }
    }

    public <K, V> List<Map<K, V>> toMaps(DataTable dataTable, Type keyType, Type valueType) {
        try {
            xStream.setParameterInfo(parameterInfo);
            SingleValueConverter keyConverter = xStream.getSingleValueConverter(keyType);
            SingleValueConverter valueConverter = xStream.getSingleValueConverter(valueType);

            if (keyConverter == null || valueConverter == null) {
                throw new CucumberException(String.format("Can't convert DataTable to List<Map<%s,%s>>", keyType, valueType));
            }

            List<Map<K, V>> result = new ArrayList<Map<K, V>>();
            List<String> keyStrings = dataTable.topCells();
            List<K> keys = new ArrayList<K>();
            for (String keyString : keyStrings) {
                keys.add((K) keyConverter.fromString(keyString));
            }
            List<List<String>> valueRows = dataTable.cells(1);
            for (List<String> valueRow : valueRows) {
                Map<K, V> map = new LinkedHashMap<K, V>();
                int i = 0;
                for (String cell : valueRow) {
                    map.put(keys.get(i), (V) valueConverter.fromString(cell));
                    i++;
                }
                result.add(Collections.unmodifiableMap(map));
            }
            return Collections.unmodifiableList(result);
        } finally {
            xStream.unsetParameterInfo();
        }
    }

    /**
     * Converts a List of objects to a DataTable.
     *
     * @param objects     the objects to convert
     * @param columnNames an explicit list of column names
     * @return a DataTable
     */
    public DataTable toTable(List<?> objects, String... columnNames) {
        try {
            xStream.setParameterInfo(parameterInfo);

            List<String> header = null;
            List<List<String>> valuesList = new ArrayList<List<String>>();
            for (Object object : objects) {
                CellWriter writer;
                if (isListOfSingleValue(object)) {
                    // XStream needs an instance of ArrayList
                    object = new ArrayList<Object>((List<Object>) object);
                    writer = new ListOfSingleValueWriter();
                } else if (isArrayOfSingleValue(object)) {
                    // XStream needs an instance of ArrayList
                    object = new ArrayList<Object>(asList((Object[]) object));
                    writer = new ListOfSingleValueWriter();
                } else if (object instanceof Map) {
                    writer = new MapWriter(asList(columnNames));
                } else {
                    writer = new ComplexTypeWriter(asList(columnNames));
                }
                xStream.marshal(object, writer);
                if (header == null) {
                    header = writer.getHeader();
                }
                List<String> values = writer.getValues();
                valuesList.add(values);
            }
            return createDataTable(header, valuesList);
        } finally {
            xStream.unsetParameterInfo();
        }
    }

    private DataTable createDataTable(List<String> header, List<List<String>> valuesList) {
        List<PickleRow> gherkinRows = new ArrayList<PickleRow>();
        if (header != null) {
            gherkinRows.add(gherkinRow(header));
        }
        for (List<String> values : valuesList) {
            gherkinRows.add(gherkinRow(values));
        }
        return new DataTable(new PickleTable(gherkinRows), this);
    }

    private PickleRow gherkinRow(List<String> cells) {
        List<PickleCell> pickleCells = new ArrayList<PickleCell>(cells.size());
        for (String cell : cells) {
            PickleCell pickleCell = new PickleCell(null, cell);
            pickleCells.add(pickleCell);
        }
        return new PickleRow(pickleCells);
    }

    private List<String> convertTopCellsToFieldNames(DataTable dataTable) {
        final StringConverter mapper = new CamelCaseStringConverter();
        return map(dataTable.topCells(), new Mapper<String, String>() {
            @Override
            public String map(String attributeName) {
                return mapper.map(attributeName);
            }
        });
    }

    private boolean isListOfSingleValue(Object object) {
        if (object instanceof List) {
            List list = (List) object;
            return !list.isEmpty() && xStream.getSingleValueConverter(list.get(0).getClass()) != null;
        }
        return false;
    }

    private boolean isArrayOfSingleValue(Object object) {
        if (object.getClass().isArray()) {
            Object[] array = (Object[]) object;
            return array.length > 0 && xStream.getSingleValueConverter(array[0].getClass()) != null;
        }
        return false;
    }
}
