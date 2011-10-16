package cucumber.table;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;

import java.util.List;
import java.util.Map;

public class TableConverter {
    private final XStream xStream;

    public TableConverter(XStream xStream) {
        this.xStream = xStream;
    }

    public <T> List<T> convert(Class itemType, List<String> attributeNames, List<List<String>> attributeValues) {
        HierarchicalStreamReader reader;
        if (Map.class.isAssignableFrom(itemType)) {
            reader = new XStreamMapReader(attributeNames, attributeValues);
        } else {
            reader = new XStreamObjectReader(itemType, attributeNames, attributeValues);
        }
        return (List) xStream.unmarshal(reader);
    }
}
