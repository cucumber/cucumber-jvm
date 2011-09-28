package cucumber.table;

import gherkin.formatter.model.Row;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Table {

    private final List<List<String>> raw;
    private final List<Row> gherkinRows;
    private final TableConverter tableConverter;
    private final TableHeaderMapper tableHeaderMapper;

    public Table(List<Row> gherkinRows, TableConverter tableConverter, TableHeaderMapper tableHeaderMapper) {
        this.gherkinRows = gherkinRows;
        this.tableConverter = tableConverter;
        this.tableHeaderMapper = tableHeaderMapper;
        this.raw = new ArrayList<List<String>>();
        for (Row row : gherkinRows) {
            List<String> list = new ArrayList<String>();
            list.addAll(row.getCells());
            this.raw.add(list);
        }
    }

    public List<List<String>> raw() {
        return this.raw;
    }

    public <T> List<T> asList(Type listType) {
        return tableConverter.convert(listType, attributeNames(), attributeValues());
    }

    private List<List<String>> attributeValues() {
        List<List<String>> attributeValues = new ArrayList<List<String>>();
        List<Row> valueRows = gherkinRows.subList(1, gherkinRows.size());
        for (Row valueRow : valueRows) {
            attributeValues.add(toStrings(valueRow));
        }
        return attributeValues;
    }

    private List<String> attributeNames() {
        List<String> strings = new ArrayList<String>();
        for (String string : gherkinRows.get(0).getCells()) {
            strings.add(tableHeaderMapper.map(string));
        }
        return strings;
    }

    private List<String> toStrings(Row row) {
        List<String> strings = new ArrayList<String>();
        for (String string : row.getCells()) {
            strings.add(string);
        }
        return strings;
    }


    public void diff(Table other) {
        new TableDiffer(this, other).calculateDiffs();
    }

    public List<Row> getGherkinRows() {
        return gherkinRows;
    }

    List<DiffableRow> diffableRows() {
        List<DiffableRow> result = new ArrayList<DiffableRow>();
        List<List<String>> convertedRows = raw();
        for (int i = 0; i < convertedRows.size(); i++) {
            result.add(new DiffableRow(getGherkinRows().get(i), convertedRows.get(i)));
        }
        return result;
    }

    TableConverter getTableConverter() {
        return tableConverter;
    }

    TableHeaderMapper getTableHeaderMapper() {
        return tableHeaderMapper;
    }

    class DiffableRow {
        public final Row row;
        public final List<String> convertedRow;

        public DiffableRow(Row row, List<String> convertedRow) {
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
