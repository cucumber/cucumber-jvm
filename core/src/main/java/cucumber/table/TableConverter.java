package cucumber.table;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import cucumber.runtime.CucumberException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class TableConverter {
    private final XStream xStream;

    public TableConverter(XStream xStream) {
        this.xStream = xStream;
    }

    public <T> List<T> convert(Type itemType, List<String> attributeNames, List<List<String>> attributeValues) {
        HierarchicalStreamReader reader;
        if (isMapOfStringToStringAssignable(itemType)) {
            reader = new XStreamMapReader(attributeNames, attributeValues);
        } else {
            reader = new XStreamObjectReader(itemType, attributeNames, attributeValues);
        }
        return (List) xStream.unmarshal(reader);
    }

    private boolean isMapOfStringToStringAssignable(Type type) {
        if(type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type rawType = parameterizedType.getRawType();
            if(rawType instanceof Class && Map.class.isAssignableFrom((Class) rawType)) {
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                boolean isStringKey = actualTypeArguments[0].equals(String.class);
                if(!isStringKey) {
                    throw new CucumberException("Tables can only be transformed to a List<Map<K,V>> when K is String. It was " + actualTypeArguments[0].toString());
                }
                boolean isStringAssignableValue = actualTypeArguments[1] instanceof Class && ((Class)actualTypeArguments[1]).isAssignableFrom(String.class);
                if(!isStringAssignableValue) {
                    throw new CucumberException("Tables can only be transformed to a List<Map<K,V>> when V is String or Object. It was " + actualTypeArguments[1].toString());
                }
                return true;
            }
            return false;
        } else {
            return false;
        }
    }
}
