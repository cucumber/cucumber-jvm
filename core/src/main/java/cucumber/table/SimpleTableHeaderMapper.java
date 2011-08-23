package cucumber.table;

import java.util.Map;

public class SimpleTableHeaderMapper implements TableHeaderMapper {

    private final Map<String, String> mappings;
    
    public SimpleTableHeaderMapper(Map<String, String> mappings) {
        this.mappings = mappings;
    }

    @Override
    public String map(String originalHeaderName) {
        return getMappings().get(originalHeaderName);
    }

    public Map<String, String> getMappings() {
        return this.mappings;
    }
}
