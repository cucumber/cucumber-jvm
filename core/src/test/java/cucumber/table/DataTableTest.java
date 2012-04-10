package cucumber.table;

import com.thoughtworks.xstream.XStream;
import cucumber.runtime.converters.LocalizedXStreams;
import gherkin.I18n;
import gherkin.formatter.model.Comment;
import gherkin.formatter.model.DataTableRow;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

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

    public DataTable createSimpleTable() {
        List<DataTableRow> simpleRows = new ArrayList<DataTableRow>();
        simpleRows.add(new DataTableRow(new ArrayList<Comment>(), asList("one", "four", "seven"), 1));
        simpleRows.add(new DataTableRow(new ArrayList<Comment>(), asList("4444", "55555", "666666"), 2));
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        XStream xStream = new LocalizedXStreams(classLoader).get(new I18n("en"));
        return new DataTable(simpleRows, new TableConverter(xStream));
    }
}
