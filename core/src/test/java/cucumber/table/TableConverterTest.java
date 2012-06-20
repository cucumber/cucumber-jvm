package cucumber.table;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class TableConverterTest {

    @Test
    public void converts_table_of_single_column_to_list_of_integers() {
        DataTable table = TableParser.parse("|3|\n|5|\n", null);
        assertEquals(asList(3, 5), table.<List<Integer>>convert(new TypeReference<List<Integer>>() {
        }.getType()));
    }

    @Test
    public void converts_table_of_several_columns_to_list_of_integers() {
        DataTable table = TableParser.parse("|3|5|\n|6|7|\n", null);
        assertEquals(asList(3, 5, 6, 7), table.<List<Integer>>convert(new TypeReference<List<Integer>>() {
        }.getType()));
    }

    @Test
    public void converts_table_of_single_column_to_list_of_list_of_integers() {
        DataTable table = TableParser.parse("|3|\n|5|\n", null);
        assertEquals(asList(asList(3), asList(5)), table.<List<List<Integer>>>convert(new TypeReference<List<List<Integer>>>() {
        }.getType()));
    }

    @Test
    public void does_not_convert_when_type_is_unspecified() {
        DataTable table = TableParser.parse("|3|5|\n|6|7|\n", null);
        assertEquals(table, table.<DataTable>convert(null));
    }

    public static enum Color {
        RED, GREEN, BLUE
    }

    @Test
    public void converts_table_of_single_column_to_enums() {
        DataTable table = TableParser.parse("|RED|\n|GREEN|\n", null);
        assertEquals(asList(Color.RED, Color.GREEN), table.<List<Integer>>convert(new TypeReference<List<Color>>() {
        }.getType()));
    }

    @Test
    public void converts_to_map_of_enum_to_int() {
        DataTable table = TableParser.parse("|RED|BLUE|\n|6|7|\n|8|9|\n", null);
        HashMap<Color, Integer> map1 = new HashMap<Color, Integer>() {{
            put(Color.RED, 6);
            put(Color.BLUE, 7);
        }};
        HashMap<Color, Integer> map2 = new HashMap<Color, Integer>() {{
            put(Color.RED, 8);
            put(Color.BLUE, 9);
        }};
        assertEquals(asList(map1, map2), table.<Map<Color, Integer>>convert(new TypeReference<List<Map<Color, Integer>>>() {
        }.getType()));
    }

    public static class UserPojo {
        private Date birthDate;
        private Calendar deathCal;
    }

    @Test
    public void converts_to_list_of_pojo() {
        DataTable table = TableParser.parse("|Birth Date|Death Cal|\n|1957-05-10|1979-02-02|\n", "yyyy-MM-dd");
        List<UserPojo> converted = table.convert(new TypeReference<List<UserPojo>>() {
        }.getType());
        assertEquals(sidsBirthday(), converted.get(0).birthDate);
        assertEquals(sidsDeathcal(), converted.get(0).deathCal);
    }

    @Test
    public void converts_to_list_of_map_of_date() {
        DataTable table = TableParser.parse("|Birth Date|Death Cal|\n|1957-05-10|1979-02-02|\n", "yyyy-MM-dd");
        List<Map<String, Date>> converted = table.convert(new TypeReference<List<Map<String, Date>>>() {
        }.getType());
        assertEquals(sidsBirthday(), converted.get(0).get("Birth Date"));
    }

    @Test
    public void converts_to_non_generic_map() {
        DataTable table = TableParser.parse("|Birth Date|Death Cal|\n|1957-05-10|1979-02-02|\n", null);
        List<Map> converted = table.convert(new TypeReference<List<Map>>() {
        }.getType());
        assertEquals("1957-05-10", converted.get(0).get("Birth Date"));
    }

    private Date sidsBirthday() {
        Calendar sidsBirthday = Calendar.getInstance();
        sidsBirthday.set(1957, 4, 10, 0, 0, 0);
        sidsBirthday.set(Calendar.MILLISECOND, 0);
        return sidsBirthday.getTime();
    }

    private Calendar sidsDeathcal() {
        Calendar sidsDeathcal = Calendar.getInstance();
        sidsDeathcal.set(1979, 1, 2, 0, 0, 0);
        sidsDeathcal.set(Calendar.MILLISECOND, 0);
        return sidsDeathcal;
    }
}
