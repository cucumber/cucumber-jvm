package cucumber.table;

import com.thoughtworks.xstream.converters.ErrorWriter;
import com.thoughtworks.xstream.io.AbstractReader;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Adapts rows in a table to an XStream Reader so that XStream can turn it into a List of objects.
 */
class XStreamTableReader extends AbstractReader {
    private final Class elementType;
    private final List<String> attributeNames;
    private final Iterator<List<String>> itemIterator;

    private int depth = 0;
    private Iterator<String> attributeNameIterator;
    private String attributeName;

    private Iterator<String> attributeValueIterator;
    private String attributeValue;

    public XStreamTableReader(Class elementType, List<String> attributeNames, List<List<String>> items) {
        this.elementType = elementType;
        this.attributeNames = attributeNames;
        this.itemIterator = items.iterator();
    }

    @Override
    public boolean hasMoreChildren() {
        switch(depth) {
            case 0:
                return itemIterator.hasNext();
            case 1:
                return attributeNameIterator.hasNext();
            case 2: 
                return false;
            default: throw new IllegalStateException("Depth is " + depth);
        }
    }

    @Override
    public void moveDown() {
        depth++;
        switch(depth) {
            case 1:
                attributeNameIterator = attributeNames.iterator();
                attributeValueIterator = itemIterator.next().iterator();
                break;
            case 2:                
                attributeName = attributeNameIterator.next();
                attributeValue = attributeValueIterator.next();
                break;
            default: throw new IllegalStateException("Depth is " + depth);
        }
    }

    @Override
    public void moveUp() {
        depth--;
    }

    @Override
    public String getNodeName() {
        switch(depth) {
            case 0: return "list";
            case 1: return elementType.getName();
            case 2: return attributeName;
            default: throw new IllegalStateException("Depth is " + depth);
        }
    }

    @Override
    public String getValue() {
        return attributeValue;
    }

    @Override
    public String getAttribute(String name) {
        return null;
    }

    @Override
    public String getAttribute(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getAttributeCount() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getAttributeName(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator getAttributeNames() {
        return Collections.emptyList().iterator();
    }

    @Override
    public void appendErrors(ErrorWriter errorWriter) {
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException();
    }
}
