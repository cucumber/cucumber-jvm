package cucumber.table;

import com.thoughtworks.xstream.XStream;

import java.util.List;

public class TableConverter {
    private final XStream xStream;

    public TableConverter(XStream xStream) {
        if(xStream == null) {
            throw new NullPointerException("xStream");
        }
        this.xStream = xStream;
    }

    public List convert(Class itemType, List<String> attributeNames, List<List<String>> attributeValues) {
        return (List) xStream.unmarshal(new XStreamTableReader(itemType, attributeNames, attributeValues));
    }
}
