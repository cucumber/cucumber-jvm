package cucumber.runtime.xstream;

import cucumber.runtime.CucumberException;

import java.util.ArrayList;
import java.util.List;

public class ListOfSingleValueWriter extends CellWriter {
    private int nodeDepth;
    private final List<String> columnNames;
    private final List<String> values = new ArrayList<String>();
    
    public ListOfSingleValueWriter() {
    	columnNames = null;
    }
    
    public ListOfSingleValueWriter(List<String> columnNames) {
        this.columnNames = columnNames;
    }
	
	@Override
	public List<String> getHeader() {
		return columnNames;
	}
    @Override
    public List<String> getValues() {
        return values;
    }

    @Override
    public void startNode(String name) {
        if (nodeDepth > 1) {
            throw new CucumberException("Can only convert List<List<T>> to a table when T is a single value (primitive, string, date etc).");
        }
        nodeDepth++;
    }

    @Override
    public void addAttribute(String name, String value) {
    }

    @Override
    public void setValue(String value) {
        values.add(value == null ? "" : value);
    }

    @Override
    public void endNode() {
        nodeDepth--;
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
