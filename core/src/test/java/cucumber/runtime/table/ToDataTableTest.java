package cucumber.runtime.table;

import cucumber.api.DataTable;
import cucumber.runtime.CucumberException;
import cucumber.runtime.ParameterInfo;
import cucumber.runtime.xstream.LocalizedXStreams;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
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
        List<UserPojo> users = tc.toList(personTable(), UserPojo.class);
        DataTable table = tc.toTable(users);
        assertEquals("" +
                "      | credits | name        | birthDate  |\n" +
                "      | 1,000   | Sid Vicious | 10/05/1957 |\n" +
                "      | 3,000   | Frank Zappa | 21/12/1940 |\n" +
                "", table.toString());
    }

    @Test
    public void converts_only_selected_fields_of_object_to_table() throws ParseException {
        RelationPojo relation = new RelationPojo();
        relation.id = 12;
        relation.user = new UserPojo(0);
        relation.user.credits = 1000;
        relation.user.name = "Tom Scott";
        relation.user.birthDate = new SimpleDateFormat("yyyy-MM-dd").parse("1984-01-01");
        relation.created = new SimpleDateFormat("yyyy-MM-dd").parse("2000-01-01");
        relation.tags = new HashSet<String>(Arrays.asList("A","B", "C"));

        DataTable table = tc.toTable(singletonList(relation), "id", "created");
        assertEquals("" +
            "      | id | created    |\n" +
            "      | 12 | 01/01/2000 |\n" +
            "", table.toString());
    }

    @Test
    public void throws_exception_when_converting_complex_selected_field_to_table() throws ParseException {
        RelationPojo relation = new RelationPojo();
        relation.id = 12;
        relation.user = new UserPojo(0);
        relation.user.credits = 1000;
        relation.user.name = "Tom Scott";
        relation.user.birthDate = new SimpleDateFormat("yyyy-MM-dd").parse("1984-01-01");
        relation.created = new SimpleDateFormat("yyyy-MM-dd").parse("2000-01-01");
        relation.tags = new HashSet<String>(Arrays.asList("A","B", "C"));

        try {
            tc.toTable(singletonList(relation), "id", "created", "tags");
            fail();
        } catch (CucumberException expected){
            assertEquals("" +
                    "Don't know how to convert \"cucumber.runtime.table.ToDataTableTest$RelationPojo.tags\" into a table entry.\n" +
                    "Either exclude tags from the table by selecting the fields to include:\n" +
                    "\n" +
                    "DataTable.create(entries, \"Field\", \"Other Field\")\n" +
                    "\n" +
                    "Or try writing your own converter:\n" +
                    "\n" +
                    "@cucumber.deps.com.thoughtworks.xstream.annotations.XStreamConverter(TagsConverter.class)\n" +
                    "public Set tags;\n",
                expected.getMessage());
        }
    }


    @Test
    public void converts_list_of_beans_with_null_to_table() {
        List<UserPojo> users = tc.toList(personTableWithNull(), UserPojo.class);
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
            tc.toList(TableParser.parse("" +
                    "| name        | birthDate  | crapola  |\n" +
                    "| Sid Vicious | 10/05/1957 | 1,000    |\n" +
                    "| Frank Zappa | 21/12/1940 | 3,000    |\n" +
                    "", PARAMETER_INFO),
                    UserPojo.class);
            fail();
        } catch (CucumberException e) {
            assertEquals("No such field cucumber.runtime.table.ToDataTableTest$UserPojo.crapola", e.getMessage());
        }
    }

    @Test
    public void gives_explicit_error_message_on_field_name_missing_in_header() {
        try {
            tc.toList(TableParser.parse("" +
                    "| name        |            |\n" +
                    "| Sid Vicious | 10/05/1957 |\n" +
                    "| Frank Zappa | 21/12/1940 |\n" +
                    "", PARAMETER_INFO),
                    UserPojo.class);
            fail();
        } catch (CucumberException e) {
            assertEquals("Field name cannot be empty. Please check the table header.", e.getMessage());
        }
    }

    @Test
    public void gives_a_nice_error_message_when_primitive_field_is_null() {
        try {
            tc.toList(TableParser.parse("" +
                    "| credits     |\n" +
                    "| 5           |\n" +
                    "|             |\n" +
                    "", PARAMETER_INFO),
                    PojoWithInt.class
            );
            fail();
        } catch (CucumberException e) {
            assertEquals("Can't assign null value to one of the primitive fields in cucumber.runtime.table.ToDataTableTest$PojoWithInt. Please use boxed types.", e.getMessage());
        }
    }

    @Test
    public void gives_a_meaningfull_error_message_when_field_is_repeated() {
        try {
            tc.toList(TableParser.parse("" +
                    "| credits     | credits     |\n" +
                    "| 5           | 5           |\n" +
                    "", PARAMETER_INFO),
                    UserPojo.class
            );
            fail();
        } catch (CucumberException e) {
            assertEquals("Duplicate field credits", e.getMessage());
        }
    }

    @Test
    public void converts_list_of_beans_to_table_with_explicit_columns() {
        List<UserPojo> users = tc.toList(personTable(), UserPojo.class);
        DataTable table = tc.toTable(users, "name", "birthDate", "credits");
        assertEquals("" +
                "      | name        | birthDate  | credits |\n" +
                "      | Sid Vicious | 10/05/1957 | 1,000   |\n" +
                "      | Frank Zappa | 21/12/1940 | 3,000   |\n" +
                "", table.toString());
    }

    @Test
    public void diffs_round_trip() {
        List<UserPojo> users = tc.toList(personTable(), UserPojo.class);
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
        List<List<Double>> actual = tc.toLists(table, Double.class);
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

    @Test
    public void enum_value_should_be_null_when_text_omitted_for_pojo() {
        final List<PojoWithEnum> actual = tc.toList(TableParser.parse("" +
                "| agree | \n" +
                "|  yes  | \n" +
                "|       | \n" +
                "", PARAMETER_INFO),
                PojoWithEnum.class
        );
        assertEquals("[PojoWithEnum{yes}, PojoWithEnum{null}]", actual.toString());
    }

    @Test
    public void mixed_case_enum_members_shall_still_work_even_when_starts_from_lower_case() {
        final List<PojoWithEnum> actual = tc.toList(TableParser.parse("" +
                "| agree            | \n" +
                "| mayBeMixedCase  | \n" +
                "", PARAMETER_INFO),
                PojoWithEnum.class
        );
        assertEquals("[PojoWithEnum{mayBeMixedCase}]", actual.toString());
    }

    @Test
    public void enum_value_should_be_null_when_text_omitted_for_plain_enum() {
        final List<AnEnum> actual = tc.toList(TableParser.parse("" +
                "| yes | \n" +
                "|     | \n" +
                "", PARAMETER_INFO),
                AnEnum.class
        );
        assertEquals("[yes, null]", actual.toString());
    }

    // No setters
    public static class RelationPojo {
        public Integer id;
        public UserPojo user;
        public Date created;
        public Set<String> tags = new HashSet<String>();

        public RelationPojo() {
        }
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

    public enum AnEnum {
        yes, no, mayBeMixedCase
    }

    public static class PojoWithEnum {
        public AnEnum agree;

        public PojoWithEnum(AnEnum agree) {
            this.agree = agree;
        }

        @Override
        public String toString() {
            return "PojoWithEnum{" + agree + '}';
        }
    }
}
