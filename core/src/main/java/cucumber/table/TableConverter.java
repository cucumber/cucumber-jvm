package cucumber.table;

import cucumber.runtime.CucumberException;
import cucumber.runtime.converters.LocalizedXStreams;
import cucumber.runtime.xstream.converters.ConversionException;
import cucumber.runtime.xstream.converters.SingleValueConverter;
import cucumber.runtime.xstream.converters.reflection.AbstractReflectionConverter;
import cucumber.runtime.xstream.io.HierarchicalStreamReader;
import cucumber.table.xstream.CellWriter;
import cucumber.table.xstream.ComplexTypeWriter;
import cucumber.table.xstream.ListOfComplexTypeReader;
import cucumber.table.xstream.ListOfSingleValueWriter;
import gherkin.formatter.model.Comment;
import gherkin.formatter.model.DataTableRow;
import gherkin.util.Mapper;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cucumber.runtime.Utils.listItemType;
import static cucumber.runtime.Utils.mapKeyType;
import static cucumber.runtime.Utils.mapValueType;
import static gherkin.util.FixJava.map;
import static java.util.Arrays.asList;

/**
 * This class converts a {@link DataTable to various other types}
 */
public class TableConverter {
    private static final List<Comment> NO_COMMENTS = Collections.emptyList();
    private final LocalizedXStreams.LocalizedXStream xStream;
    private final String dateFormat;

    public TableConverter(LocalizedXStreams.LocalizedXStream xStream, String dateFormat) {
        this.xStream = xStream;
        this.dateFormat = dateFormat;
    }

    public <T> T convert(Type type, DataTable dataTable) {
        try {
            xStream.setDateFormat(dateFormat);
            if (type == null || (type instanceof Class && ((Class) type).isAssignableFrom(DataTable.class))) {
                return (T) dataTable;
            }

            Type itemType = listItemType(type);
            if (itemType == null) {
                throw new CucumberException("Not a List type: " + type);
            }

            Type listItemType = listItemType(itemType);
            if (listItemType == null) {
                SingleValueConverter singleValueConverter = xStream.getSingleValueConverter(itemType);
                if (singleValueConverter != null) {
                    return (T) toListOfSingleValue(dataTable, singleValueConverter);
                } else {
                    if (itemType instanceof Class) {
                        if (Map.class.equals(itemType)) {
                            // Non-generic map
                            SingleValueConverter mapKeyConverter = xStream.getSingleValueConverter(String.class);
                            SingleValueConverter mapValueConverter = xStream.getSingleValueConverter(String.class);
                            return (T) toListOfSingleValueMap(dataTable, mapKeyConverter, mapValueConverter);
                        } else {
                            return (T) toListOfComplexType(dataTable, (Class) itemType);
                        }
                    } else {
                        SingleValueConverter mapKeyConverter = xStream.getSingleValueConverter(mapKeyType(itemType));
                        SingleValueConverter mapValueConverter = xStream.getSingleValueConverter(mapValueType(itemType));
                        if (mapKeyConverter != null && mapValueConverter != null) {
                            return (T) toListOfSingleValueMap(dataTable, mapKeyConverter, mapValueConverter);
                        } else {
                            throw new CucumberException("Can't convert a table to " + type + ". When using List<SomeComplexType>, SomeComplexType must not be a generic type");
                        }
                    }
                }
            } else {
                SingleValueConverter singleValueConverter = xStream.getSingleValueConverter(listItemType);
                if (singleValueConverter != null) {
                    return (T) toListOfListOfSingleValue(dataTable, singleValueConverter);
                } else {
                    // List<List<Something>>
                    throw new CucumberException("Can't convert to " + type.toString());
                }
            }
        } finally {
            xStream.unsetDateFormat();
        }
    }

    private <T> List<T> toListOfComplexType(DataTable dataTable, Class<T> itemType) {
        HierarchicalStreamReader reader = new ListOfComplexTypeReader(itemType, convertTopCellsToFieldNames(dataTable), dataTable.cells(1));
        try {
            return Collections.unmodifiableList((List<T>) xStream.unmarshal(reader));
        } catch (AbstractReflectionConverter.UnknownFieldException e) {
            throw new CucumberException(e.getShortMessage());
        } catch (ConversionException e) {
            throw new CucumberException(String.format("Can't assign null value to one of the primitive fields in %s. Please use boxed types.", e.get("class")));
        }
    }

    private List<Object> toListOfSingleValue(DataTable dataTable, SingleValueConverter singleValueConverter) {
        List<Object> result = new ArrayList<Object>();
        for (String cell : dataTable.flatten()) {
            result.add(singleValueConverter.fromString(cell));
        }
        return Collections.unmodifiableList(result);
    }

    private List<List<Object>> toListOfListOfSingleValue(DataTable dataTable, SingleValueConverter singleValueConverter) {
        List<List<Object>> result = new ArrayList<List<Object>>();
        for (List<String> row : dataTable.raw()) {
            List<Object> convertedRow = new ArrayList<Object>();
            for (String cell : row) {
                convertedRow.add(singleValueConverter.fromString(cell));
            }
            result.add(Collections.unmodifiableList(convertedRow));
        }
        return Collections.unmodifiableList(result);
    }

    private List<Map<Object, Object>> toListOfSingleValueMap(DataTable dataTable, SingleValueConverter mapKeyConverter, SingleValueConverter mapValueConverter) {
        List<Map<Object, Object>> result = new ArrayList<Map<Object, Object>>();
        List<String> keyStrings = dataTable.topCells();
        List<Object> keys = new ArrayList<Object>();
        for (String keyString : keyStrings) {
            keys.add(mapKeyConverter.fromString(keyString));
        }
        List<List<String>> valueRows = dataTable.cells(1);
        for (List<String> valueRow : valueRows) {
            Map<Object, Object> map = new HashMap<Object, Object>();
            int i = 0;
            for (String cell : valueRow) {
                map.put(keys.get(i), mapValueConverter.fromString(cell));
                i++;
            }
            result.add(Collections.unmodifiableMap(map));
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Converts a DataTable to a List of objects.
     */
    public <T> List<T> toList(final Type type, DataTable dataTable) {
        if (type == null) {
            return convert(new GenericListType(new GenericListType(String.class)), dataTable);
        }
        return convert(new GenericListType(type), dataTable);
    }

    /**
     * Converts a List of objects to a DataTable.
     *
     * @param objects     the objects to convert
     * @param columnNames an explicit list of column names (currently not used)
     * @return a DataTable
     */
    public DataTable toTable(List<?> objects, String... columnNames) {
        try {
            xStream.setDateFormat(dateFormat);

            List<String> header = null;
            List<List<String>> valuesList = new ArrayList<List<String>>();
            for (Object object : objects) {
                CellWriter writer;
                if (isListOfSingleValue(object)) {
                    // XStream needs this
                    object = new ArrayList<Object>((List<Object>) object);
                    writer = new ListOfSingleValueWriter();
                } else {
                    writer = new ComplexTypeWriter(asList(columnNames));
                }
                xStream.marshal(object, writer);
                if(header == null) {
                    header = writer.getHeader();
                }
                List<String> values = writer.getValues();
                valuesList.add(values);
            }
            return createDataTable(header, valuesList);
        } finally {
            xStream.unsetDateFormat();
        }
    }

    private DataTable createDataTable(List<String> header, List<List<String>> valuesList) {
        List<DataTableRow> gherkinRows = new ArrayList<DataTableRow>();
        if(header != null) {
            gherkinRows.add(gherkinRow(header));
        }
        for (List<String> values : valuesList) {
            gherkinRows.add(gherkinRow(values));
        }
        return new DataTable(gherkinRows, this);
    }

    private DataTableRow gherkinRow(List<String> cells) {
        return new DataTableRow(NO_COMMENTS, cells, 0);
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
            boolean isSingleValue = xStream.getSingleValueConverter(list.get(0).getClass()) != null;
            if (list.size() > 0 && isSingleValue) {
                return true;
            }
        }
        return false;
    }

    private static class GenericListType implements ParameterizedType {
        private final Type type;

        public GenericListType(Type type) {
            this.type = type;
        }

        @Override
        public Type[] getActualTypeArguments() {
            return new Type[]{type};
        }

        @Override
        public Type getRawType() {
            return List.class;
        }

        @Override
        public Type getOwnerType() {
            throw new UnsupportedOperationException();
        }
    }
}
