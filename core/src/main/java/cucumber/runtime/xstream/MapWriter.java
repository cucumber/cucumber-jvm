package cucumber.runtime.xstream;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Supports Map&lt;String, Object> as the List item
 * 
 * @author Nicholas Albion
 */
public class MapWriter extends CellWriter {
	private String key;
	private final List<String> columnNames;
    private final Map<String, Object> values = new LinkedHashMap<String, Object>();
    private final List<String> fieldValues = new ArrayList<String>();
    
    public MapWriter(List<String> columnNames) {
        this.columnNames = columnNames;
    }
	
	@Override
	public List<String> getHeader() {
		return columnNames;
	}

	@Override
    public List<String> getValues() {
		if (columnNames.size() > 0) {
			List<String> fieldValues = new ArrayList<String>(columnNames.size());
            for (String columnName : columnNames) {
            	Object value = values.get(columnName);
                fieldValues.add( value == null ? "" : value.toString() );
            }
            
            return fieldValues;
        } else {
            return fieldValues;
        }
    }
	
    @Override
    public void setValue(String value) {
    	if( key == null ) {
    		key = value;
    	} else {
    		values.put(key, value);
    		fieldValues.add(value == null ? "" : value);
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
