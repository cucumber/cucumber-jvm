package cucumber.table;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.SingleValueConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import cucumber.runtime.CucumberException;
import cucumber.table.xstream.*;
import gherkin.util.Mapper;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import static gherkin.util.FixJava.map;

public class TableConverter {
    private final XStream xStream;

    public TableConverter(XStream xStream) {
        this.xStream = xStream;
    }

    public <T> List<T> convert(Type itemType, List<String> attributeNames, List<List<String>> attributeValues) {
        HierarchicalStreamReader reader;
        ensureNotNonGenericMap(itemType);
        if (isMapOfStringToStringAssignable(itemType)) {
            reader = new XStreamMapListReader(attributeNames, attributeValues);
        } else {
            final StringConverter mapper = new CamelCaseStringConverter();
            attributeNames = map(attributeNames, new Mapper<String, String>() {
                @Override
                public String map(String attributeName) {
                    return mapper.map(attributeName);
                }
            });
            reader = new XStreamObjectListReader(itemType, attributeNames, attributeValues);
        }
        return (List) xStream.unmarshal(reader);
    }

    public DataTable convert(List<?> objects) {
        XStreamTableWriter writer;
        Converter converter = xStream.getConverterLookup().lookupConverterForType(objects.get(0).getClass());
        if(converter instanceof SingleValueConverter) {
            writer = new XStreamSingleValueListWriter(this);
        } else {
            writer = new XStreamObjectListWriter(this);
        }
        xStream.marshal(objects, writer);
        return writer.getDataTable();
    }

    private void ensureNotNonGenericMap(Type type) {
        if (type instanceof Class && Map.class.isAssignableFrom((Class<?>) type)) {
            throw new CucumberException("Tables can only be transformed to List<Map<String,String>> or List<Map<String,Object>>. You have to declare generic types.");
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
}
