package cucumber.table;

import gherkin.formatter.model.Row;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cucumber.runtime.transformers.Transformer;

public class Table {

    private final List<List<String>> raw;
    private List<String> headers;
    private Map<String, Transformer<?>> columnTransformersByHeader;
    private Map<Integer, Transformer<?>> columnTransformers;
    private TableHeaderMapper headerMapper;
    private final Locale locale;

    public Table(List<Row> gherkinRows, Locale locale) {
        this.locale = locale;
        this.raw = new ArrayList<List<String>>();
        for (Row row : gherkinRows) {
            List<String> list = new ArrayList<String>();
            list.addAll(row.getCells());
            this.raw.add(list);
        }
    }

    /**
     * 
     * @return the headers of the table (first <i>raw</i> row with labels)
     */
    public List<String> getHeaders() {
        if (this.headers == null) {
            this.headers = transformHeaders(this.raw.get(0)); 
        }
        return this.headers;
    }

    private List<String> transformHeaders(List<String> rawHeaders) {
        List<String> transformedHeaders = new ArrayList<String>();
        for (String rawHeader : rawHeaders) {
            String transformedHeader = getHeaderMapper().map(rawHeader);
            if (transformedHeader != null) {
                transformedHeaders.add(transformedHeader);
            } else {
                transformedHeaders.add(rawHeader);
            }
        }
        return transformedHeaders;
    }

    public List<List<String>> raw() {
        return this.raw;
    }
    /**
     * 
     * @return a List of Row, with each each cell value transformed 
     */
    public List<List<Object>> rows() {
        List<List<Object>> rows = new ArrayList<List<Object>>();
        for (List<String> rawRow : getRawRows()) {
            List<Object> newRow = new ArrayList<Object>();
            for (int i=0; i<rawRow.size();i++) {
                newRow.add(transformCellValue(i, rawRow.get(i)));
            }
            rows.add(newRow);
        }
        return rows;
    }
    
    private List<List<String>> getRawRows() {
        return this.raw.subList(1, this.raw.size());
    }

    public List<Map<String, Object>> hashes() {
        List<Map<String, Object>> hashes = new ArrayList<Map<String, Object>>();
        List<List<Object>> rows = rows();
        for (List<Object> row : rows) {
            Map<String, Object> map = new HashMap<String, Object>();
            for (int i = 0; i < row.size(); i++) {
                map.put(getHeaders().get(i), row.get(i));
            }
            hashes.add(map);
        }
        return hashes;
    }

    private Object transformCellValue(int colPos, String cellValue) {
        Object hashValue;
        Transformer<?> transformer = getColumnTransformer(colPos);
        if (transformer != null) {
            hashValue = transformer.transform(this.locale, cellValue);
        } else {
            hashValue = cellValue;
        }
        return hashValue;
    }

    private Transformer<?> getColumnTransformer(String header) {
        return getColumnTransformersByHeader().get(header);
    }
    
    private Transformer<?> getColumnTransformer(int colPos) {
        return getColumnTransformers().get(colPos);
    }

    public void mapColumn(String columnName, Transformer<?> transformer) {
        getColumnTransformersByHeader().put(columnName, transformer);
    }
    
    public void mapColumn(int columnIndex, Transformer<?> transformer) {
        getColumnTransformers().put(columnIndex, transformer);
    }

    public void mapHeaders(TableHeaderMapper mapper) {
        this.headerMapper = mapper;
    }

    public TableHeaderMapper getHeaderMapper() {
        if(this.headerMapper == null) {
            this.headerMapper = new NoOpTableHeaderMapper();
        }
        return this.headerMapper;
    }

    public Map<Integer, Transformer<?>> getColumnTransformers() {
        if (this.columnTransformers == null) {
            this.columnTransformers = createColumnTransformers();
        }
        return this.columnTransformers;
    }

    private Map<Integer, Transformer<?>> createColumnTransformers() {
        Map<Integer, Transformer<?>> transformersMap = new HashMap<Integer, Transformer<?>>();
        if (this.columnTransformersByHeader != null) {
            for (int i = 0; i < getHeaders().size(); i++) {
                Transformer<?> transformer = getColumnTransformer(getHeaders().get(i));
                if (transformer != null) {
                    transformersMap.put(i, transformer);
                }
            }
        }
        return transformersMap;
    }

    public Map<String, Transformer<?>> getColumnTransformersByHeader() {
        if (this.columnTransformersByHeader == null) {
            this.columnTransformersByHeader = new HashMap<String, Transformer<?>>();
        }
        return this.columnTransformersByHeader;
    }
    
    public void diff(Table other) {
        new TableDiffer(this, other).calculateDiffs();
    }
}
