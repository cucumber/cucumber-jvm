package cucumber.runtime;

import cucumber.api.TypeRegistry;
import cucumber.api.datatable.DataTable;
import cucumber.api.datatable.DataTableType;
import cucumber.api.datatable.TableCellTransformer;
import cucumber.api.datatable.TableEntryTransformer;
import cucumber.api.datatable.TableParser;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class TableConverterTest {

    private final TypeRegistry registry = new TypeRegistry(Locale.ENGLISH);
    private final TypeRegistryTableConverter converter = new TypeRegistryTableConverter(registry);

    @Test
    public void converts_table_of_single_column_to_list_of_integers() {
        DataTable table = TableParser.parse("|3|\n|5|\n|6|\n|7|\n", converter);
        assertEquals(asList(3, 5, 6, 7), table.asList(Integer.class));
    }

    @Test
    public void converts_table_of_two_columns_to_map() {
        DataTable table = TableParser.parse("|3|c|\n|5|e|\n|6|f|\n", converter);
        Map<Integer, String> expected = new HashMap<Integer, String>() {{
            put(3, "c");
            put(5, "e");
            put(6, "f");
        }};

        assertEquals(expected, table.asMap(Integer.class, String.class));
    }

    public static class WithoutStringConstructor {
        public String count;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            WithoutStringConstructor thingie = (WithoutStringConstructor) o;

            if (!count.equals(thingie.count)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return count.hashCode();
        }

        @Override
        public String toString() {
            return "Thingie{" +
                "count=" + count +
                '}';
        }

        public WithoutStringConstructor val(String s) {
            count = s;
            return this;
        }
    }

    @Test
    public void converts_table_of_single_column_to_list_of_without_string_constructor() {
        registry.defineDataTableType(
            new DataTableType("withoutStringConstructor",
                WithoutStringConstructor.class,
                new TableEntryTransformer<WithoutStringConstructor>() {
                    @Override
                    public WithoutStringConstructor transform(Map<String, String> tableRow) {
                        return new WithoutStringConstructor().val(tableRow.get("count"));
                    }
                }));

        DataTable table = TableParser.parse("|count|\n|5|\n|6|\n|7|\n", converter);
        List<WithoutStringConstructor> expected = asList(
            new WithoutStringConstructor().val("5"),
            new WithoutStringConstructor().val("6"),
            new WithoutStringConstructor().val("7"));
        assertEquals(expected, table.asList(WithoutStringConstructor.class));
    }

    public enum Color {
        RED, GREEN, BLUE
    }

    @Test
    public void converts_to_map_of_enum_to_int() {
        registry.defineDataTableType(new DataTableType("color", Color.class, new TableCellTransformer<Color>() {
            @Override
            public Color transform(String cell) {
                return Color.valueOf(cell);
            }
        }));

        DataTable table = TableParser.parse("|RED|BLUE|\n|6|7|\n|8|9|\n", converter);
        HashMap<Color, Integer> map1 = new HashMap<Color, Integer>() {{
            put(Color.RED, 6);
            put(Color.BLUE, 7);
        }};
        HashMap<Color, Integer> map2 = new HashMap<Color, Integer>() {{
            put(Color.RED, 8);
            put(Color.BLUE, 9);
        }};
        List<Map<Color, Integer>> converted = table.asMaps(Color.class, Integer.class);
        assertEquals(asList(map1, map2), converted);
    }


    @Test
    public void converts_to_list_of_map_of_string() {
        DataTable table = TableParser.parse("|Birth Date|Death Cal|\n|1957-05-10|1979-02-02|\n", converter);
        List<Map<String, String>> converted = table.asMaps(String.class, String.class);
        assertEquals("1957-05-10", converted.get(0).get("Birth Date"));
    }

    public static class FirstFromStringable {
        private final String value;

        public FirstFromStringable(String value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FirstFromStringable that = (FirstFromStringable) o;

            if (value != null ? !value.equals(that.value) : that.value != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return value != null ? value.hashCode() : 0;
        }
    }

    public static class SecondFromStringable {
        private final String value;

        public SecondFromStringable(String value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SecondFromStringable that = (SecondFromStringable) o;

            if (value != null ? !value.equals(that.value) : that.value != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return value != null ? value.hashCode() : 0;
        }
    }
}
