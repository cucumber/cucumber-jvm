package cucumber.table;

import java.util.HashMap;
import java.util.Map;

public class SimpleTableHeaderMapper implements TableHeaderMapper {

    private Map<String, String> mappings;
    
    public SimpleTableHeaderMapper() {
        super();
    }
    
    public SimpleTableHeaderMapper(Map<String, String> mappings) {
        super();
        this.mappings = mappings;
    }

    @Override
    public String map(String originalHeaderName) {
        return getMappings().get(originalHeaderName);
    }

    public void addMapping(String originalHeaderName, String newHeaderName) {
        getMappings().put(originalHeaderName, newHeaderName);
    }

    public Map<String, String> getMappings() {
        if (this.mappings == null) {
            this.mappings = new HashMap<String, String>();
        }
        return this.mappings;
    }

    public void setMappings(Map<String, String> mappings) {
        this.mappings = mappings;
    }

}
