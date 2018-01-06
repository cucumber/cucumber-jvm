package cucumber.api.datatable;

import cucumber.runtime.CucumberException;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

public class DataTableTest {

    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private TableConverter tableConverter;

    @Test
    public void emptyRowsAreIgnored() {
        DataTable table = createTable(Collections.<String>emptyList(), Collections.<String>emptyList());
        assertTrue(table.isEmpty());
        assertTrue(table.raw().isEmpty());
    }

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
        DataTable other = DataTable.create(listOfListOfString);
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

    @Test
    public void asMaps_delegates_to_converter() {
        DataTable table = createTable(asList("hundred", "100"), asList("thousand", "1000"));
        table.asMaps(String.class, Long.class);
        verify(tableConverter).toMaps(table, String.class, Long.class);
    }

    @Test
    public void asMap_delegates_to_converter() {
        DataTable table = createTable(asList("hundred", "100"), asList("thousand", "1000"));
        table.asMap(String.class, Long.class);
        verify(tableConverter).toMap(table, String.class, Long.class);
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
        List<List<String>> table = asList(rows);
        return DataTable.create(table, tableConverter);
    }
}
