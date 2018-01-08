package io.cucumber.datatable;

import io.cucumber.cucumberexpressions.TypeReference;
import io.cucumber.datatable.DataTable.TableConverter;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import static io.cucumber.datatable.DataTable.emptyDataTable;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class EmptyDataTableConverterTest {

    private static final Type LIST_OF_INT_TYPE = new TypeReference<List<Integer>>() {
    }.getType();
    private static final Type MAP_OF_INT_INT_TYPE = new TypeReference<Map<Integer, Integer>>() {
    }.getType();
    private static final Type LIST_OF_MAP_OF_INT_INT_TYPE = new TypeReference<List<Map<Integer, Integer>>>() {
    }.getType();
    private static final Type LIST_OF_LIST_OF_INT_TYPE = new TypeReference<List<List<Integer>>>() {
    }.getType();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private final TableConverter converter = new DataTable.EmptyDataTableConverter();
    private final DataTable table = emptyDataTable();


    @Test
    public void converts_datatable_to_datatable() {
        assertSame(table, converter.convert(table, DataTable.class, false));
    }

    @Test
    public void converts_and_transposes_datatable() {
        assertEquals(table.transpose(), converter.convert(table, DataTable.class, true));
    }

    @Test
    public void empty_table_to_null() {
        assertNull(converter.convert(table, Integer.class, false));
    }

    @Test
    public void converts_empty_table_to_empty_list() {
        DataTable table = emptyDataTable();
        assertEquals(emptyList(), converter.toList(table, Integer.class));
        assertEquals(emptyList(), converter.convert(table, LIST_OF_INT_TYPE, false));
    }

    @Test
    public void converts_empty_table_to_empty_lists() {
        DataTable table = emptyDataTable();
        assertEquals(emptyList(), converter.toLists(table, Integer.class));
        assertEquals(emptyList(), converter.convert(table, LIST_OF_LIST_OF_INT_TYPE, false));
    }

    @Test
    public void converts_empty_table_to_empty_map() {
        DataTable table = emptyDataTable();
        assertEquals(emptyMap(), converter.toMap(table, Integer.class, Integer.class));
        assertEquals(emptyMap(), converter.convert(table, MAP_OF_INT_INT_TYPE, false));
    }

    @Test
    public void converts_table_to_maps() {
        assertEquals(emptyList(), converter.toMaps(table, Integer.class, Integer.class));
        assertEquals(emptyList(), converter.convert(table, LIST_OF_MAP_OF_INT_INT_TYPE, false));
    }
}
