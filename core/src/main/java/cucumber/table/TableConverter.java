package cucumber.table;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import java.util.List;

public class TableConverter {
    // We don't use any of XStream's the XML stuff (only its Converter functionality).
    // The default ctor requires more classes we don't have/need.
    private final XStream x = new XStream(new DomDriver());

    public List convert(Class itemType, List<String> attributeNames, List<List<String>> attributeValues) {
        return (List) x.unmarshal(new XStreamTableReader(itemType, attributeNames, attributeValues));
    }
}
