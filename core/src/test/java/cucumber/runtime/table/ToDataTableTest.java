package cucumber.runtime.table;

import cucumber.api.DataTable;
import cucumber.runtime.CucumberException;
import cucumber.runtime.ParameterInfo;
import cucumber.runtime.xstream.LocalizedXStreams;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ToDataTableTest {
    private static final String DD_MM_YYYY = "dd/MM/yyyy";
    private static final ParameterInfo PARAMETER_INFO = new ParameterInfo(null, DD_MM_YYYY, null, null);
    private TableConverter tc;

    @Before
    public void createTableConverterWithDateFormat() {
        LocalizedXStreams.LocalizedXStream xStream = new LocalizedXStreams(Thread.currentThread().getContextClassLoader()).get(Locale.US);
        tc = new TableConverter(xStream, new ParameterInfo(null, DD_MM_YYYY, null, null));
    }

    @Test
    public void converts_list_of_beans_to_table() {
        List<UserPojo> users = tc.toList(UserPojo.class, personTable());
        DataTable table = tc.toTable(users);
        assertEquals("" +
                "      | credits | name        | birthDate  |\n" +
                "      | 1,000   | Sid Vicious | 10/05/1957 |\n" +
                "      | 3,000   | Frank Zappa | 21/12/1940 |\n" +
                "", table.toString());
    }

    @Test
    public void converts_list_of_beans_with_null_to_table() {
        List<UserPojo> users = tc.toList(UserPojo.class, personTableWithNull());
        DataTable table = tc.toTable(users, "name", "birthDate", "credits");
        assertEquals("" +
                "      | name        | birthDate  | credits |\n" +
                "      | Sid Vicious |            | 1,000   |\n" +
                "      | Frank Zappa | 21/12/1940 | 3,000   |\n" +
                "", table.toString());
    }

    @Test
    public void gives_a_nice_error_message_when_field_is_missing() {
        try {
            tc.toList(UserPojo.class, TableParser.parse("" +
                    "| name        | birthDate  | crapola  |\n" +
                    "| Sid Vicious | 10/05/1957 | 1,000    |\n" +
                    "| Frank Zappa | 21/12/1940 | 3,000    |\n" +
                    "", PARAMETER_INFO)
            );
            fail();
        } catch (CucumberException e) {
            assertEquals("No such field cucumber.runtime.table.ToDataTableTest$UserPojo.crapola", e.getMessage());
        }
    }

    @Test
    public void gives_a_nice_error_message_when_primitive_field_is_null() {
        try {
            tc.toList(PojoWithInt.class, TableParser.parse("" +
                    "| credits     |\n" +
                    "| 5           |\n" +
                    "|             |\n" +
                    "", PARAMETER_INFO)
            );
            fail();
        } catch (CucumberException e) {
            assertEquals("Can't assign null value to one of the primitive fields in cucumber.runtime.table.ToDataTableTest$PojoWithInt. Please use boxed types.", e.getMessage());
        }
    }

    @Test
    public void gives_a_meaningfull_error_message_when_field_is_repeated() {
        try {
            tc.toList(UserPojo.class, TableParser.parse("" +
                    "| credits     | credits     |\n" +
                    "| 5           | 5           |\n" +
                    "", PARAMETER_INFO)
            );
            fail();
        } catch (CucumberException e) {
            assertEquals("Duplicate field credits", e.getMessage());
        }
    }

    @Test
    public void converts_list_of_beans_to_table_with_explicit_columns() {
        List<UserPojo> users = tc.toList(UserPojo.class, personTable());
        DataTable table = tc.toTable(users, "name", "birthDate", "credits");
        assertEquals("" +
                "      | name        | birthDate  | credits |\n" +
                "      | Sid Vicious | 10/05/1957 | 1,000   |\n" +
                "      | Frank Zappa | 21/12/1940 | 3,000   |\n" +
                "", table.toString());
    }

    @Test
    public void diffs_round_trip() {
        List<UserPojo> users = tc.toList(UserPojo.class, personTable());
        personTable().diff(users);
    }

    private DataTable personTable() {
        return TableParser.parse("" +
                "| name        | birthDate  | credits  |\n" +
                "| Sid Vicious | 10/05/1957 | 1,000    |\n" +
                "| Frank Zappa | 21/12/1940 | 3,000    |\n" +
                "", PARAMETER_INFO);
    }

    private DataTable personTableWithNull() {
        return TableParser.parse("" +
                "| name        | birthDate  | credits  |\n" +
                "| Sid Vicious |            | 1,000    |\n" +
                "| Frank Zappa | 21/12/1940 | 3,000    |\n" +
                "", PARAMETER_INFO);
    }

    @Test
    public void converts_list_of_list_of_number_to_table() {
        List<? extends List<? extends Number>> lists = asList(asList(0.5, 1.5), asList(99.0, 1000.5));
        DataTable table = tc.toTable(lists);
        assertEquals("" +
                "      | 0.5 | 1.5     |\n" +
                "      | 99  | 1,000.5 |\n" +
                "", table.toString());
        Type listOfDoubleType = new TypeReference<List<Double>>() {
        }.getType();
        List<List<Double>> actual = tc.toList(listOfDoubleType, table);
        assertEquals(lists, actual);
    }

    @Test
    public void converts_list_of_array_of_string_or_number_to_table_with_number_formatting() {
        List<Object[]> arrays = asList(
                new Object[]{"name", "birthDate", "credits"},
                new Object[]{"Sid Vicious", "10/05/1957", 1000},
                new Object[]{"Frank Zappa", "21/12/1940", 3000}
        );
        DataTable table = tc.toTable(arrays);
        assertEquals("" +
                "      | name        | birthDate  | credits |\n" +
                "      | Sid Vicious | 10/05/1957 | 1,000   |\n" +
                "      | Frank Zappa | 21/12/1940 | 3,000   |\n" +
                "", table.toString());
    }

    @Test
    public void convert_list_of_maps_to_table() {
        Map<String, Object> vicious = new LinkedHashMap<String, Object>();
        vicious.put("name", "Sid Vicious");
        vicious.put("birthDate", "10/05/1957");
        vicious.put("credits", 1000);
        Map<String, Object> zappa = new LinkedHashMap<String, Object>();
        zappa.put("name", "Frank Zappa");
        zappa.put("birthDate", "21/12/1940");
        zappa.put("credits", 3000);
        List<Map<String, Object>> maps = asList(vicious, zappa);

        assertEquals("" +
                "      | name        | credits | birthDate  |\n" +
                "      | Sid Vicious | 1,000   | 10/05/1957 |\n" +
                "      | Frank Zappa | 3,000   | 21/12/1940 |\n" +
                "", tc.toTable(maps, "name", "credits", "birthDate").toString());

        assertEquals("" +
                "      | name        | birthDate  | credits |\n" +
                "      | Sid Vicious | 10/05/1957 | 1,000   |\n" +
                "      | Frank Zappa | 21/12/1940 | 3,000   |\n" +
                "", tc.toTable(maps).toString());
    }

    // No setters
    public static class UserPojo {
        public Integer credits;
        public String name;
        public Date birthDate;

        public UserPojo(int foo) {
        }
    }

    public static class PojoWithInt {
        public int credits;
    }
}
