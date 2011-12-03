package cucumber.table.xstream;

import cucumber.table.DataTable;
import cucumber.table.TableConverter;
import gherkin.formatter.model.Comment;
import gherkin.formatter.model.DataTableRow;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;

public class ListOfObjectWriter extends DataTableWriter {
    private static final List<Comment> NO_COMMENTS = emptyList();
    private final List<DataTableRow> rows = new ArrayList<DataTableRow>();
    private final TableConverter tableConverter;
    private int nodeDepth = 0;
    private List<String> header = new ArrayList<String>();
    private List<String> cells;

    public ListOfObjectWriter(TableConverter tableConverter) {
        this.tableConverter = tableConverter;
    }

    @Override
    public DataTable getDataTable() {
        return new DataTable(rows, tableConverter);
    }

    @Override
    public void startNode(String name) {
        nodeDepth++;
        if (nodeDepth == 2) {
            cells = new ArrayList<String>();
        }
        if (nodeDepth == 3 && header != null) {
            header.add(name);
        }
    }

    @Override
    public void addAttribute(String name, String value) {
    }

    @Override
    public void setValue(String text) {
        cells.add(text);
    }

    @Override
    public void endNode() {
        if (nodeDepth == 2) {
            if (header != null) {
                DataTableRow headerRow = new DataTableRow(NO_COMMENTS, header, 0);
                rows.add(headerRow);
                header = null;
            }
            DataTableRow row = new DataTableRow(NO_COMMENTS, cells, 0);
            rows.add(row);
        }
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
