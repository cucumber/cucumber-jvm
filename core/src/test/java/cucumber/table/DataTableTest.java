package cucumber.table;

import cucumber.runtime.converters.LocalizedXStreams;
import gherkin.formatter.model.Comment;
import gherkin.formatter.model.DataTableRow;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class DataTableTest {

    @Test
    public void rawShouldHaveThreeColumnsAndTwoRows() {
        List<List<String>> raw = createSimpleTable().raw();
        assertEquals("Rows size", 2, raw.size());
        for (List<String> list : raw) {
            assertEquals("Cols size: " + list, 3, list.size());
        }
    }

    @Test
    public void canCreateTableFromListOfListOfString() {
        DataTable dataTable = createSimpleTable();
        DataTable other = dataTable.toTable(dataTable.raw());
        assertEquals("" +
                "      | one  | four  | seven  |\n" +
                "      | 4444 | 55555 | 666666 |\n",
                other.toString());
    }

    @Test
    public void canCreateTableFromListOfListOfStringWithoutOtherTable() {
        DataTable dataTable = createSimpleTable();
        DataTable other = DataTable.create(dataTable.raw());
        assertEquals("" +
                "      | one  | four  | seven  |\n" +
                "      | 4444 | 55555 | 666666 |\n",
                other.toString());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void raw_row_is_immutable() {
        createSimpleTable().raw().remove(0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void raw_col_is_immutable() {
        createSimpleTable().raw().get(0).remove(0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void asMaps_is_immutable() {
        createSimpleTable().asMaps().remove(0);
    }

    public DataTable createSimpleTable() {
        List<DataTableRow> simpleRows = new ArrayList<DataTableRow>();
        simpleRows.add(new DataTableRow(new ArrayList<Comment>(), asList("one", "four", "seven"), 1));
        simpleRows.add(new DataTableRow(new ArrayList<Comment>(), asList("4444", "55555", "666666"), 2));
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        LocalizedXStreams.LocalizedXStream xStream = new LocalizedXStreams(classLoader).get(Locale.US);
        return new DataTable(simpleRows, new TableConverter(xStream, null));
    }
}
