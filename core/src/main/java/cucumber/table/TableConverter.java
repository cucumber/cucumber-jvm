package cucumber.table;

import com.thoughtworks.xstream.XStream;

import java.lang.reflect.Type;
import java.util.List;

public class TableConverter {
    private final XStream xStream;

    public TableConverter(XStream xStream) {
        this.xStream = xStream;
    }

    public <T> List<T> convert(Type listType, List<String> attributeNames, List<List<String>> attributeValues) {
        return (List) xStream.unmarshal(new XStreamTableReader(listType, attributeNames, attributeValues));
    }
}
