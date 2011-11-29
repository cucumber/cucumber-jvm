package cucumber.table.xstream;

import com.thoughtworks.xstream.converters.ErrorWriter;
import com.thoughtworks.xstream.io.AbstractReader;

import java.util.Iterator;
import java.util.List;

/**
 * <pre>
 * <list>
 *   <list>
 *     <int>100</int>
 *   </list>
 *   <list>
 *     <int>100</int>
 *   </list>
 * </list>
 * </pre>
 */
public class XStreamSingleValueListReader extends AbstractReader {
    private int depth = 0;
    private Iterator<List<String>> rowIterator;
    private Iterator<String> cellIterator;
    private String cell;
    private final Class elementType;

    public XStreamSingleValueListReader(Class elementType, List<List<String>> rows) {
        this.elementType = elementType;
        rowIterator = rows.iterator();
    }

    @Override
    public boolean hasMoreChildren() {
        switch (depth) {
            case 0:
                return rowIterator.hasNext();
            case 1:
                return cellIterator.hasNext();
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
                cellIterator = rowIterator.next().iterator();
                break;
            case 2:
                cell = cellIterator.next();
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
            case 1:
                return "list";
            case 2:
                return elementType.getName();
            default:
                throw new IllegalStateException("Depth is " + depth);
        }
    }

    @Override
    public String getValue() {
        return cell;
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
        throw new UnsupportedOperationException();
    }

    @Override
    public void appendErrors(ErrorWriter errorWriter) {
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException();
    }
}
