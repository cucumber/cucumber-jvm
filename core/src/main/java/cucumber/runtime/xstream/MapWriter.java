package cucumber.runtime.xstream;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Supports Map&lt;String, Object&gt; as the List item
 */
public class MapWriter extends CellWriter {
    private final List<String> columnNames;
    private final Map<String, Object> values = new LinkedHashMap<String, Object>();

    private String key;

    public MapWriter(List<String> columnNames) {
        this.columnNames = columnNames;
    }

    @Override
    public List<String> getHeader() {
        return columnNames.isEmpty() ? new ArrayList<String>(values.keySet()) : columnNames;
    }

    @Override
    public List<String> getValues() {
        List<String> values = new ArrayList<String>(columnNames.size());
        for (String columnName : getHeader()) {
            Object value = this.values.get(columnName);
            values.add(value == null ? "" : value.toString());
        }
        return values;
    }

    @Override
    public void setValue(String value) {
        if (key == null) {
            key = value;
        } else {
            values.put(key, value);
            key = null;
        }
    }

    @Override
    public void flush() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void startNode(String name) {
    }

    @Override
    public void addAttribute(String name, String value) {
    }

    @Override
    public void endNode() {
    }
}