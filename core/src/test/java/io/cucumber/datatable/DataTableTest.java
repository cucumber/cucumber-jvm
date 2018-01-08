package io.cucumber.datatable;

import io.cucumber.datatable.DataTable.TableConverter;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
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
    public void emptyTableIsEmpty() {
        DataTable table = DataTable.emptyDataTable();
        assertTrue(table.isEmpty());
        assertTrue(table.raw().isEmpty());
    }

    @Test
    public void rawShouldEqualRaw() {
        List<List<String>> raw = asList(asList("hundred", "100"), asList("thousand", "1000"));
        DataTable table = DataTable.create(raw);
        assertEquals(raw, table.raw());
    }

    @Test
    public void cellsShouldEqualRaw() {
        List<List<String>> raw = asList(asList("hundred", "100"), asList("thousand", "1000"));
        DataTable table = DataTable.create(raw);
        assertEquals(raw, table.cells());
    }

    @Test
    public void cellShouldGetFromRaw() {
        List<List<String>> raw = asList(asList("hundred", "100"), asList("thousand", "1000"));
        DataTable table = DataTable.create(raw);
        assertEquals(raw.get(0).get(0), table.cell(0, 0));
        assertEquals(raw.get(0).get(1), table.cell(1, 0));
        assertEquals(raw.get(1).get(0), table.cell(0, 1));
        assertEquals(raw.get(1).get(1), table.cell(1, 1));
    }

    @Test
    public void rowsShouldViewSubSetOfRows() {
        List<List<String>> raw = asList(
            asList("ten", "10"),
            asList("hundred", "100"),
            asList("thousand", "1000")
        );

        DataTable table = DataTable.create(raw);

        assertEquals(
            asList(
                asList("hundred", "100"),
                asList("thousand", "1000")),
            table.rows(1));

        assertEquals(
            singletonList(
                asList("hundred", "100")),
            table.rows(1, 2));
    }

    @Test
    public void columnsShouldViewSubSetOfColumns() {
        List<List<String>> raw = asList(
            asList("hundred", "100", "2"),
            asList("thousand", "1000", "3"));

        DataTable table = DataTable.create(raw);

        assertEquals(
            asList(
                asList("100", "2"),
                asList("1000", "3")),
            table.columns(1));

        assertEquals(
            asList(
                singletonList("100"),
                singletonList("1000")),
            table.columns(1, 2));
    }

    @Test
    public void asListsShouldEqualRaw() {
        List<List<String>> raw = asList(asList("hundred", "100"), asList("thousand", "1000"));
        DataTable table = DataTable.create(raw);
        assertEquals(raw, table.asLists());
    }

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

    @Test(expected = IllegalArgumentException.class)
    public void canNotSupportNonRectangularTablesMissingColumn() {
        createTable(asList("one", "four", "seven"),
            asList("a1", "a4444"),
            asList("b1")).raw();
    }

    @Test(expected = IllegalArgumentException.class)
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
    public void convert_delegates_to_converter() {
        DataTable table = createTable(asList("1", "100"), asList("2", "1000"));
        table.convert(Long.class, false);
        verify(tableConverter).convert(table, Long.class, false);
    }

    @Test
    public void asList_delegates_to_converter() {
        DataTable table = createTable(asList("1", "100"), asList("2", "1000"));
        table.asList(Long.class);
        verify(tableConverter).toList(table, Long.class);
    }

    @Test
    public void asLists_delegates_to_converter() {
        DataTable table = createTable(asList("1", "100"), asList("2", "1000"));
        table.asLists(Long.class);
        verify(tableConverter).toLists(table, Long.class);
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

    @Test
    public void can_print_table_to_appendable() throws IOException {
        DataTable table = createSimpleTable();
        Appendable appendable = new StringBuilder();
        table.print(appendable);
        String expected = "" +
            "      | one  | four  | seven  |\n" +
            "      | 4444 | 55555 | 666666 |\n";
        assertEquals(expected, appendable.toString());
    }

    @Test
    public void can_print_table_to_string_builder() {
        DataTable table = createSimpleTable();
        StringBuilder appendable = new StringBuilder();
        table.print(appendable);
        String expected = "" +
            "      | one  | four  | seven  |\n" +
            "      | 4444 | 55555 | 666666 |\n";
        assertEquals(expected, appendable.toString());
    }

    private DataTable createSimpleTable() {
        return createTable(asList("one", "four", "seven"), asList("4444", "55555", "666666"));
    }

    private DataTable createTable(List<String>... rows) {
        List<List<String>> table = asList(rows);
        return DataTable.create(table, tableConverter);
    }
}
