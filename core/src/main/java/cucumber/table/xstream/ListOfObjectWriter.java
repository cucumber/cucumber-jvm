package cucumber.table.xstream;

import cucumber.table.DataTable;
import cucumber.table.TableConverter;
import gherkin.formatter.model.Comment;
import gherkin.formatter.model.DataTableRow;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

public class ListOfObjectWriter extends DataTableWriter {
    private static final List<Comment> NO_COMMENTS = emptyList();
    private final List<DataTableRow> rows = new ArrayList<DataTableRow>();
    private final TableConverter tableConverter;
    private final List<String> fieldNames;

    private int nodeDepth = 0;
    private String[] fieldValues;
    private int fieldIndex = -1;

    public ListOfObjectWriter(TableConverter tableConverter, String... columnNames) {
        this.tableConverter = tableConverter;
        fieldNames = asList(columnNames);

        DataTableRow headerRow = new DataTableRow(NO_COMMENTS, fieldNames, 0);
        rows.add(headerRow);
    }

    @Override
    public DataTable getDataTable() {
        return new DataTable(rows, tableConverter);
    }

    @Override
    public void startNode(String name) {
        nodeDepth++;
        if (nodeDepth == 2) {
            fieldValues = new String[fieldNames.size()];
        }
        if (nodeDepth == 3) {
            fieldIndex = fieldNames.indexOf(name);
        }
    }

    @Override
    public void addAttribute(String name, String value) {
    }

    @Override
    public void setValue(String text) {
        if(fieldIndex != -1) {
            fieldValues[fieldIndex] = text;
        }
    }

    @Override
    public void endNode() {
        if (nodeDepth == 2) {
            List<String> cells = toArrayReplacingNullWithEmptyString();
            DataTableRow row = new DataTableRow(NO_COMMENTS, cells, 0);
            rows.add(row);
        }
        nodeDepth--;
    }

    private List<String> toArrayReplacingNullWithEmptyString() {
        List<String> cells = new ArrayList<String>(fieldValues.length);
        for(int i = 0; i < fieldValues.length; i++) {
            String fieldValue = fieldValues[i];
            if(fieldValue == null) {
                fieldValue = "";
            }
            cells.add(fieldValue);
        }
        return cells;
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
