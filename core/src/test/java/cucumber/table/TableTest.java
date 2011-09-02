package cucumber.table;

import gherkin.formatter.model.Comment;
import gherkin.formatter.model.Row;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class TableTest {

    private Table simpleTable;

    @Before
    public void initSimpleTable() {
        List<Row> simpleRows = new ArrayList<Row>();
        String[] firstLine = new String[]{"one", "four", "seven"};
        simpleRows.add(new Row(new ArrayList<Comment>(), asList(firstLine), 1));
        simpleRows.add(new Row(new ArrayList<Comment>(), asList("4444", "55555", "666666"), 2));
        simpleTable = new Table(simpleRows, Locale.getDefault());
    }

    @Test
    public void rawShouldHaveThreeColumnsAndTwoRows() {
        List<List<String>> raw = simpleTable.raw();
        assertEquals("Rows size", 2, raw.size());
        for (List<String> list : raw) {
            assertEquals("Cols size: " + list, 3, list.size());
        }
    }
}
