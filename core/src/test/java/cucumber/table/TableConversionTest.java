package cucumber.table;

import org.junit.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class TableConversionTest {

    @Test
    public void converts_table_of_single_column_to_list_of_integers() {
        DataTable table = TableParser.parse("|3|\n|5|\n", null);
        List<Integer> integers = table.asList(Integer.class);
        assertEquals(asList(3, 5), integers);
    }

    @Test
    public void converts_table_of_single_column_to_list_of_list_of_integers() {
        DataTable table = TableParser.parse("|3|\n|5|\n", null);
        List<Integer> integers = table.asList(new TypeReference<List<Integer>>() {
        }.getType());
        assertEquals(asList(asList(3), asList(5)), integers);
    }

    public static enum Color {
        RED, GREEN, BLUE
    }

    @Test
    public void converts_table_of_single_column_to_enums() {
        DataTable table = TableParser.parse("|RED|\n|GREEN|\n", null);
        List<Integer> integers = table.asList(Color.class);
        assertEquals(asList(Color.RED, Color.GREEN), integers);
    }
}
