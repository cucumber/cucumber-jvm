package cucumber.table;

import com.thoughtworks.xstream.XStream;
import cucumber.runtime.converters.LocalizedXStreams;
import gherkin.formatter.model.Comment;
import gherkin.formatter.model.DataTableRow;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class DataTableTest {

    private DataTable simpleTable;

    @Before
    public void initSimpleTable() {
        List<DataTableRow> simpleRows = new ArrayList<DataTableRow>();
        simpleRows.add(new DataTableRow(new ArrayList<Comment>(), asList("one", "four", "seven"), 1));
        simpleRows.add(new DataTableRow(new ArrayList<Comment>(), asList("4444", "55555", "666666"), 2));
        XStream xStream = new LocalizedXStreams().get(Locale.UK);
        simpleTable = new DataTable(simpleRows, new TableConverter(xStream));
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
