package cucumber.runtime.xstream;

import cucumber.runtime.table.CamelCaseStringConverter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class ComplexTypeWriter extends CellWriter {
    private final List<String> columnNames;
    private Map<String, String> fields = new LinkedHashMap<String, String>();
    private String currentKey;

    private int nodeDepth = 0;

    public ComplexTypeWriter(List<String> columnNames) {
        this.columnNames = columnNames;
    }

    @Override
    public List<String> getHeader() {
        return columnNames.isEmpty() ? new ArrayList<String>(fields.keySet()) : columnNames;
    }

    @Override
    public List<String> getValues() {
        CamelCaseStringConverter converter = new CamelCaseStringConverter();
        if (!columnNames.isEmpty()) {
            String[] explicitFieldValues = new String[columnNames.size()];
            int n = 0;
            for (String columnName : columnNames) {
                final String convertedColumnName = converter.map(columnName);
                if (fields.containsKey(convertedColumnName)) {
                    explicitFieldValues[n] = fields.get(convertedColumnName);
                } else {
                    explicitFieldValues[n] = "";
                }
                n++;
            }
            return asList(explicitFieldValues);
        } else {
            return new ArrayList<String>(fields.values());
        }
    }

    @Override
    public void startNode(String name) {
        if (nodeDepth == 1) {
            currentKey = name;
        }
        nodeDepth++;
    }

    @Override
    public void addAttribute(String name, String value) {
    }

    @Override
    public void setValue(String value) {
        fields.put(currentKey, value == null ? "" : value);
    }

    @Override
    public void endNode() {
        nodeDepth--;
        currentKey = null;
    }

    @Override
    public void flush() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException();
    }
}
