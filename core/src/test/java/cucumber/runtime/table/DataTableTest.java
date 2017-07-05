package cucumber.runtime.table;

import cucumber.api.DataTable;
import cucumber.runtime.CucumberException;
import cucumber.runtime.xstream.LocalizedXStreams;
import gherkin.pickles.PickleCell;
import gherkin.pickles.PickleRow;
import gherkin.pickles.PickleTable;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;

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
    public void transposedRawShouldHaveTwoColumnsAndThreeRows() {
        List<List<String>> raw = createSimpleTable().transpose().raw();
        assertEquals("Rows size", 3, raw.size());
        for (List<String> list : raw) {
            assertEquals("Cols size: " + list, 2, list.size());
        }
    }

    @Test(expected = CucumberException.class)
    public void canNotSupportNonRectangularTablesMissingColumn() {
        createTable(asList("one", "four", "seven"),
                asList("a1", "a4444"),
                asList("b1")).raw();
    }

    @Test(expected = CucumberException.class)
    public void canNotSupportNonRectangularTablesExceedingColumn() {
        createTable(asList("one", "four", "seven"),
                asList("a1", "a4444", "b7777777", "zero")).raw();
    }

    @Test
    public void canCreateTableFromListOfListOfString() {
        DataTable dataTable = createSimpleTable();
        List<List<String>> listOfListOfString = dataTable.raw();
        DataTable other = dataTable.toTable(listOfListOfString);
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
        List<Map<String, String>> maps = createSimpleTable().asMaps(String.class, String.class);
        maps.remove(0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void asMap_is_immutable() {
        Map<String, Long> map = createTable(asList("hundred", "100"), asList("thousand", "1000")).asMap(String.class, Long.class);
        assertEquals(new Long(1000L), map.get("thousand"));
        map.remove("hundred");
    }

    @Test
    public void two_identical_tables_are_considered_equal() {
        assertEquals(createSimpleTable(), createSimpleTable());
        assertEquals(createSimpleTable().hashCode(), createSimpleTable().hashCode());
    }

    @Test
    public void two_identical_transposed_tables_are_considered_equal() {
        assertEquals(createSimpleTable().transpose(), createSimpleTable().transpose());
        assertEquals(createSimpleTable().transpose().hashCode(), createSimpleTable().transpose().hashCode());
    }

    @Test
    public void two_different_tables_are_considered_non_equal() {
        assertFalse(createSimpleTable().equals(createTable(asList("one"))));
        assertNotSame(createSimpleTable().hashCode(), createTable(asList("one")).hashCode());
    }

    @Test
    public void two_different_transposed_tables_are_considered_non_equal() {
        assertFalse(createSimpleTable().transpose().equals(createTable(asList("one")).transpose()));
        assertNotSame(createSimpleTable().transpose().hashCode(), createTable(asList("one")).transpose().hashCode());
    }

    public DataTable createSimpleTable() {
        return createTable(asList("one", "four", "seven"), asList("4444", "55555", "666666"));
    }

    private DataTable createTable(List<String>... rows) {
        List<PickleRow> simpleRows = new ArrayList<PickleRow>();
        for (int i = 0; i < rows.length; i++) {
            List<PickleCell> cells = new ArrayList<PickleCell>();
            for (String cellContent : rows[i]) {
                cells.add(new PickleCell(null, cellContent));
            }
            simpleRows.add(new PickleRow(cells));
        }
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        LocalizedXStreams.LocalizedXStream xStream = new LocalizedXStreams(classLoader).get(Locale.US);
        return new DataTable(new PickleTable(simpleRows), new TableConverter(xStream, null));
    }
}
