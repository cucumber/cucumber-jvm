package cucumber.table;

import cucumber.runtime.CucumberException;
import cucumber.runtime.converters.LocalizedXStreams;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ToDataTableTest {
    private static final String DD_MM_YYYY = "dd/MM/yyyy";
    private TableConverter tc;

    @Before
    public void createTableConverterWithDateFormat() {
        LocalizedXStreams.LocalizedXStream xStream = new LocalizedXStreams(Thread.currentThread().getContextClassLoader()).get(Locale.US);
        tc = new TableConverter(xStream, DD_MM_YYYY);
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
                    "", DD_MM_YYYY)
            );
            fail();
        } catch (CucumberException e) {
            assertEquals("No such field cucumber.table.ToDataTableTest$UserPojo.crapola", e.getMessage());
        }
    }

    @Test
    public void gives_a_nice_error_message_when_primitive_field_is_null() {
        try {
            tc.toList(PojoWithInt.class, TableParser.parse("" +
                    "| credits     |\n" +
                    "| 5           |\n" +
                    "|             |\n" +
                    "", DD_MM_YYYY)
            );
            fail();
        } catch (CucumberException e) {
            assertEquals("Can't assign null value to one of the primitive fields in cucumber.table.ToDataTableTest$PojoWithInt. Please use boxed types.", e.getMessage());
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
                "", DD_MM_YYYY);
    }

    private DataTable personTableWithNull() {
        return TableParser.parse("" +
                "| name        | birthDate  | credits  |\n" +
                "| Sid Vicious |            | 1,000    |\n" +
                "| Frank Zappa | 21/12/1940 | 3,000    |\n" +
                "", DD_MM_YYYY);
    }

    @Test
    public void converts_list_of_single_value_to_table() {
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
