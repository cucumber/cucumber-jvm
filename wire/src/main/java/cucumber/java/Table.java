package cucumber.java;

import cucumber.java.exception.CukeException;
import cucumber.java.exception.CukeRuntimeException;

import java.util.ArrayList;
import java.util.List;

public class Table {
    private List<String> columns = new ArrayList<String>();
    private List<List<String>> rows = new ArrayList<List<String>>();

    public void addRow(List<String> row) throws CukeException {
        rows.add(row);
    }

    public void addColumn(String column) throws CukeException {
        if (rows.isEmpty()) {
            columns.add(column);
        } else {
            throw new CukeRuntimeException("Cannot alter columns after rows have been added");
        }
    }

    public List<String> getColumns() { return columns; }

    public List<List<String>> getRows() {
        return rows;
    }
}
