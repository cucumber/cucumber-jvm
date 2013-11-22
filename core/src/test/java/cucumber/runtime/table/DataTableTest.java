package cucumber.runtime.table;

import cucumber.api.DataTable;
import cucumber.runtime.xstream.LocalizedXStreams;
import gherkin.formatter.model.Comment;
import gherkin.formatter.model.DataTableRow;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    
    @Test
    public void canTransposeNonRectangularTables() {
        List<List<String>> raw = createNonRectangularTable().transpose().raw();
        assertEquals("Rows size", 4, raw.size());
        assertEquals("Cols size: " + raw.get(0), 4, raw.get(0).size());
        assertEquals("Cols size: " + raw.get(1), 4, raw.get(1).size());
        assertEquals("Cols size: " + raw.get(2), 2, raw.get(2).size());
        assertEquals("Cols size: " + raw.get(3), 2, raw.get(3).size());
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
    
    public DataTable createNonRectangularTable() {
        return createTable(asList("one", "four", "seven"), 
        		asList("a1", "a4444", "a7777777", "zero"),
        		asList("b1"),
        		asList("c1", "c4444"));
    }

    private DataTable createTable(List<String>... rows) {
        List<DataTableRow> simpleRows = new ArrayList<DataTableRow>();
        for (int i = 0; i < rows.length; i++) {
            simpleRows.add(new DataTableRow(new ArrayList<Comment>(), rows[i], i + 1));
        }
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        LocalizedXStreams.LocalizedXStream xStream = new LocalizedXStreams(classLoader).get(Locale.US);
        return new DataTable(simpleRows, new TableConverter(xStream, null));
    }
}
