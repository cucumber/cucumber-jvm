package cucumber.table;

import cucumber.runtime.transformers.Transformer;
import gherkin.formatter.model.Row;

import java.util.*;

public class Table {

    private final List<List<String>> raw;
    private List<String> headers;
    private final Map<String, Transformer<?>> columnTransformersByHeader = new HashMap<String, Transformer<?>>();
    private Map<Integer, Transformer<?>> columnTransformers;
    private TableHeaderMapper headerMapper;
    private final Locale locale;
    private List<Row> gherkinRows;

    public Table(List<Row> gherkinRows, Locale locale) {
        this.gherkinRows = gherkinRows;
        this.locale = locale;
        this.raw = new ArrayList<List<String>>();
        for (Row row : gherkinRows) {
            List<String> list = new ArrayList<String>();
            list.addAll(row.getCells());
            this.raw.add(list);
        }
    }

    /**
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
     * @return a List of Row, with each each cell value transformed
     */
    public List<List<Object>> rows() {
        List<List<Object>> rows = new ArrayList<List<Object>>();
        for (List<String> rawRow : getRawRows()) {
            List<Object> newRow = new ArrayList<Object>();
            for (int i = 0; i < rawRow.size(); i++) {
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
        return this.columnTransformersByHeader.get(header);
    }

    private Transformer<?> getColumnTransformer(int colPos) {
        return getColumnTransformers().get(colPos);
    }

    public void mapColumn(String columnName, Transformer<?> transformer) {
        this.columnTransformersByHeader.put(columnName, transformer);
    }

    public void mapColumn(int columnIndex, Transformer<?> transformer) {
        getColumnTransformers().put(columnIndex, transformer);
    }

    public void mapHeaders(TableHeaderMapper mapper) {
        this.headerMapper = mapper;
    }

    public TableHeaderMapper getHeaderMapper() {
        if (this.headerMapper == null) {
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

    public void diff(Table other) {
        new TableDiffer(this, other).calculateDiffs();
    }

    public List<Row> getGherkinRows() {
        return gherkinRows;
    }

    public Locale getLocale() {
        return locale;
    }

    List<DiffableRow> diffableRows() {
        List<DiffableRow> result = new ArrayList<DiffableRow>();
        List<List<Object>> convertedRows = rows();
        for(int i = 0; i < convertedRows.size(); i++) {
            result.add(new DiffableRow(getGherkinRows().get(i+1), convertedRows.get(i)));
        }
        return result;
    }

    // TODO: Get rid of this class if we base the diffing on simple List<List<String>
    // from the List<Row> list passed to the ctor.
    class DiffableRow {
        public final Row row;
        public final List<Object> convertedRow;

        public DiffableRow(Row row, List<Object> convertedRow) {
            this.row = row;
            this.convertedRow = convertedRow;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DiffableRow that = (DiffableRow) o;
            return convertedRow.equals(that.convertedRow);

        }

        @Override
        public int hashCode() {
            return convertedRow.hashCode();
        }
    }
}
