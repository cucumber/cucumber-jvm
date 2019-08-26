package cucumber.runtime.xstream;

import cucumber.deps.com.thoughtworks.xstream.converters.ErrorWriter;
import cucumber.deps.com.thoughtworks.xstream.io.AbstractReader;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Generates XStream XML data from table rows that will create a List of objects. Example:
 * <pre>
 * <list>
 *     <cucumber.runtime.table.TableConverterTest_-UserPojo>
 *         <name>Sid Vicious</name>
 *         <birthDate>1957-05-10 00:00:00.0 UTC</birthDate>
 *         <credits>1000</credits>
 *     </cucumber.runtime.table.TableConverterTest_-UserPojo>
 *     <cucumber.runtime.table.TableConverterTest_-UserPojo>
 *         <name>Frank Zappa</name>
 *         <birthDate>1940-12-21 00:00:00.0 UTC</birthDate>
 *         <credits>3000</credits>
 *     </cucumber.runtime.table.TableConverterTest_-UserPojo>
 * </list>
 * </pre>
 */
public class ListOfComplexTypeReader extends AbstractReader {
    private final Class itemType;
    private final List<String> attributeNames;
    private final Iterator<List<String>> itemIterator;

    private int depth = 0;
    private Iterator<String> attributeNameIterator;
    private String attributeName;

    private Iterator<String> attributeValueIterator;
    private String attributeValue;

    public ListOfComplexTypeReader(Class itemType, List<String> attributeNames, List<List<String>> items) {
        this.itemType = itemType;
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
                return itemType.getName();
            case 2:
                return attributeName;
            default:
                throw new IllegalStateException("Depth is " + depth);
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
