package cucumber.table;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import com.thoughtworks.xstream.converters.reflection.AbstractReflectionConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import cucumber.runtime.CucumberException;
import cucumber.runtime.converters.LocalizedXStreams;
import cucumber.table.xstream.DataTableWriter;
import cucumber.table.xstream.ListOfListOfSingleValueReader;
import cucumber.table.xstream.ListOfListOfSingleValueWriter;
import cucumber.table.xstream.ListOfMapReader;
import cucumber.table.xstream.ListOfObjectReader;
import cucumber.table.xstream.ListOfObjectWriter;
import gherkin.util.Mapper;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static gherkin.util.FixJava.map;

public class TableConverter {
    private final LocalizedXStreams.LocalizedXStream xStream;
    private final String dateFormat;

    public TableConverter(LocalizedXStreams.LocalizedXStream xStream, String dateFormat) {
        this.xStream = xStream;
        this.dateFormat = dateFormat;
    }

    /**
     * Converts a DataTable to a List of objects.
     */
    public <T> List<T> toList(Type itemType, DataTable dataTable) {
        HierarchicalStreamReader reader;
        ensureNotNonGenericMap(itemType);

        Class listOfListType = listOfListType(itemType);
        if (listOfListType != null) {
            reader = new ListOfListOfSingleValueReader(listOfListType, dataTable.cells(0));
        } else if (isMapOfStringToStringAssignable(itemType)) {
            reader = new ListOfMapReader(dataTable.topCells(), dataTable.cells(1));
        } else {
            reader = new ListOfObjectReader(itemType, convertedAttributeNames(dataTable), dataTable.cells(1));
        }
        try {
            xStream.setDateFormat(dateFormat);
            return Collections.unmodifiableList((List) xStream.unmarshal(reader));
        } catch (AbstractReflectionConverter.UnknownFieldException e) {
            throw new CucumberException(e.getShortMessage());
        } catch (ConversionException e) {
            throw new CucumberException(String.format("Can't assign null value to one of the primitive fields in %s. Please use boxed types.", e.get("class")));
        } finally {
            xStream.unsetDateFormat();
        }
    }

    /**
     * Converts a List of objects to a DataTable.
     *
     * @param objects     the objects to convert
     * @param columnNames an explicit list of column names (currently not used)
     * @return a DataTable
     */
    public DataTable toTable(List<?> objects, String... columnNames) {
        // Need to wrap the list to be sure xStream behaves well
        // It doesn't like unmodifiable lists etc.
        objects = new ArrayList<Object>(objects);
        DataTableWriter writer;
        if (isListOfListOfSingleValue(objects)) {
            objects = wrapLists((List<List<?>>) objects);
            writer = new ListOfListOfSingleValueWriter(this);
        } else {
            if (columnNames.length == 0) {
                // Figure out column names by looking at class
                columnNames = fieldNames(objects.get(0).getClass());
            }
            writer = new ListOfObjectWriter(this, columnNames);
        }
        try {
            xStream.setDateFormat(dateFormat);
            xStream.marshal(objects, writer);
            return writer.getDataTable();
        } finally {
            xStream.unsetDateFormat();
        }
    }

    private String[] fieldNames(Class clazz) {
        Field[] fields = clazz.getFields();
        String[] fieldNames = new String[fields.length];
        int i = 0;
        for (Field field : fields) {
            fieldNames[i++] = field.getName();
        }
        return fieldNames;
    }

    // This is a hack to prevent XStream from outputting weird-looking "XML" for Arrays.asList() - created lists.
    private List<List<?>> wrapLists(List<List<?>> lists) {
        List<List<?>> result = new ArrayList<List<?>>();
        for (List<?> list : lists) {
            List<?> resultList = new ArrayList<Object>(list);
            result.add(resultList);
        }
        return result;
    }

    // We have to convert attribute names to valid field names.
    private List<String> convertedAttributeNames(DataTable dataTable) {
        final StringConverter mapper = new CamelCaseStringConverter();
        return map(dataTable.topCells(), new Mapper<String, String>() {
            @Override
            public String map(String attributeName) {
                return mapper.map(attributeName);
            }
        });
    }

    private boolean isListOfListOfSingleValue(List<?> objects) {
        for (Object object : objects) {
            if (object instanceof List) {
                List list = (List) object;
                if (list.size() > 0 && isSingleValue(list.get(0).getClass())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void ensureNotNonGenericMap(Type type) {
        if (type instanceof Class && Map.class.isAssignableFrom((Class<?>) type)) {
            throw new CucumberException("Tables can only be transformed to List<Map<String,String>> or List<Map<String,Object>>. You have to declare generic types.");
        }
    }

    private Class listOfListType(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type rawType = parameterizedType.getRawType();
            if (rawType instanceof Class && List.class.isAssignableFrom((Class) rawType)) {
                Type listType = parameterizedType.getActualTypeArguments()[0];
                if (listType instanceof Class) {
                    return (Class) listType;
                }
                return null;
            }
            return null;
        } else {
            return null;
        }
    }

    private boolean isMapOfStringToStringAssignable(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type rawType = parameterizedType.getRawType();
            if (rawType instanceof Class && Map.class.isAssignableFrom((Class) rawType)) {
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                boolean isStringKey = actualTypeArguments[0].equals(String.class);
                if (!isStringKey) {
                    throw new CucumberException("Tables can only be transformed to a List<Map<K,V>> when K is String. It was " + actualTypeArguments[0].toString() + ".");
                }
                boolean isStringAssignableValue = actualTypeArguments[1] instanceof Class && ((Class) actualTypeArguments[1]).isAssignableFrom(String.class);
                if (!isStringAssignableValue) {
                    throw new CucumberException("Tables can only be transformed to a List<Map<K,V>> when V is String or Object. It was " + actualTypeArguments[1].toString() + ".");
                }
                return true;
            }
            return false;
        } else {
            return false;
        }
    }

    private boolean isSingleValue(Class<?> type) {
        Converter converter = xStream.getConverterLookup().lookupConverterForType(type);
        return converter instanceof SingleValueConverter;
    }

}
