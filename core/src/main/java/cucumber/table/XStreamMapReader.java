package cucumber.table;

import com.thoughtworks.xstream.converters.ErrorWriter;
import com.thoughtworks.xstream.io.AbstractReader;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Generates XStream XML data from table rows that will create a List of objects. Example:
 * <pre>
 * <list>
 *     <map>
 *         <entry>
 *             <string>name</string>
 *             <string>Sid Vicious</string>
 *         </entry>
 *         <entry>
 *             <string>birthDate</string>
 *             <string>10/05/1957</string>
 *         </entry>
 *     </map>
 *     <map>
 *         <entry>
 *             <string>name</string>
 *             <string>Frank Zappa</string>
 *         </entry>
 *         <entry>
 *             <string>birthDate</string>
 *             <string>21/12/1940</string>
 *         </entry>
 *     </map>
 * </list>
 * </pre>
 */
public class XStreamMapReader extends AbstractReader {
    private final List<String> attributeNames;
    private final Iterator<List<String>> itemIterator;

    private int depth = 0;

    private Iterator<String> attributeNameIterator;
    private String attributeName;

    private Iterator<String> attributeValueIterator;
    private String attributeValue;

    private boolean entryKey = true;
    
    public XStreamMapReader(List<String> attributeNames, List<List<String>> items) {
        this.attributeNames = attributeNames;
        this.itemIterator = items.iterator();
    }

    @Override
    public boolean hasMoreChildren() {
        switch (depth) {
            case 0:
                return itemIterator.hasNext();
            case 1:
                return attributeNameIterator.hasNext();
            case 2:
                return true;
            case 3:
                return false;
            default:
                throw new IllegalStateException("Depth is " + depth);
        }
    }

    @Override
    public void moveDown() {
        depth++;
        switch (depth) {
            case 1:
                attributeNameIterator = attributeNames.iterator();
                attributeValueIterator = itemIterator.next().iterator();
                break;
            case 2:
                attributeName = attributeNameIterator.next();
                attributeValue = attributeValueIterator.next();
                break;
            case 3:
                break;
            default:
                throw new IllegalStateException("Depth is " + depth);
        }
    }

    @Override
    public void moveUp() {
        depth--;
    }

    @Override
    public String getNodeName() {
        switch (depth) {
            case 0:
                return "list";
            case 1:
                return "map";
            case 2:
                return "entry";
            case 3:
                return "string";
            default:
                throw new IllegalStateException("Depth is " + depth);
        }
    }

    @Override
    public String getValue() {
        String result = entryKey ? attributeName : attributeValue;
        entryKey = !entryKey;
        return result;
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
