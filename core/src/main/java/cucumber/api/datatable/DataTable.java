package cucumber.api.datatable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;

public final class DataTable {
    private final List<List<String>> raw;

    private final TableConverter tableConverter;

    public DataTable(List<List<String>> raw) {
        this(raw, new NoConverterDefined());
    }


    public DataTable(List<List<String>> raw, TableConverter tableConverter) {
        if (raw == null) throw new CucumberDataTableException("cells can not be null");
        this.raw = raw;
        this.tableConverter = tableConverter;
    }


    public List<String> topRow() {
        if (raw.isEmpty()) {
            return null;
        }

        return raw.get(0);
    }

    public List<List<String>> rows(int fromRow) {
        return rows(fromRow, raw.size());
    }

    public List<List<String>> rows(int fromRow, int toRow) {
        return raw.subList(fromRow, toRow);
    }

    public List<List<String>> cells() {
        return raw;
    }

    public DataTable transpose() {
        List<List<String>> transposed = new ArrayList<List<String>>();
        for (List<String> pickleRow : raw) {
            for (int j = 0; j < pickleRow.size(); j++) {
                List<String> row = null;
                if (j < transposed.size()) {
                    row = transposed.get(j);
                }
                if (row == null) {
                    row = new ArrayList<String>();
                    transposed.add(row);
                }
                row.add(pickleRow.get(j));
            }
        }
        return new DataTable(transposed, tableConverter);
    }

    public List<List<String>> asLists(){
        //TODO: Implement
        return null;
    }

    public List<Map<String, String>> asMaps() {
        //TODO: Make immutable
        if (raw.isEmpty()) return emptyList();

        List<String> headers = raw.get(0);
        List<Map<String, String>> headersAndRows = new ArrayList<Map<String, String>>();

        for (int i = 1; i < raw.size(); i++) {
            List<String> row = raw.get(i);
            LinkedHashMap<String, String> headersAndRow = new LinkedHashMap<String, String>();
            for (int j = 0; j < headers.size(); j++) {
                headersAndRow.put(headers.get(j), row.get(j));
            }
            headersAndRows.add(headersAndRow);
        }

        return headersAndRows;
    }



    public <T> T convert(Type type, boolean transposed) {
        return tableConverter.convert(this, type, transposed);
    }

    public <T> List<T> asList(Type itemType) {
        return tableConverter.toList(this, itemType);
    }

    public <T> List<List<T>> asLists(Type itemType) {
        return tableConverter.toLists(this, itemType);
    }

    public <K, V> Map<K, V> asMap(Type keyType, Type valueType) {
        return tableConverter.toMap(this, keyType, valueType);
    }

    public <K, V> List<Map<K, V>> asMaps(Type keyType, Type valueType) {
        return tableConverter.toMaps(this, keyType, valueType);
    }

    private static final class NoConverterDefined implements TableConverter {

        @Override
        public <T> T convert(DataTable dataTable, Type type, boolean transposed) {
            throw new CucumberDataTableException(String.format("Can't convert DataTable to %s. DataTable was created without a converter", type));
        }

        @Override
        public <T> List<T> toList(DataTable dataTable, Type itemType) {
            throw new CucumberDataTableException(String.format("Can't convert DataTable to List<%s>. DataTable was created without a converter", itemType));
        }

        @Override
        public <T> List<List<T>> toLists(DataTable dataTable, Type itemType) {
            throw new CucumberDataTableException(String.format("Can't convert DataTable to List<List<%s>>. DataTable was created without a converter", itemType));
        }

        @Override
        public <K, V> Map<K, V> toMap(DataTable dataTable, Type keyType, Type valueType) {
            throw new CucumberDataTableException(String.format("Can't convert DataTable to Map<%s,%s>. DataTable was created without a converter", keyType, valueType));
        }

        @Override
        public <K, V> List<Map<K, V>> toMaps(DataTable dataTable, Type keyType, Type valueType) {
            throw new CucumberDataTableException(String.format("Can't convert DataTable to List<Map<%s,%s>>. DataTable was created without a converter", keyType, valueType));
        }

        @Override
        public DataTable toTable(List<?> objects, String... columnNames) {
            throw new CucumberDataTableException("Can't create a DataTable. DataTable was created without a converter");
        }
    }
}