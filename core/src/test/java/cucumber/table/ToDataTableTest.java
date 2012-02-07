package cucumber.table;

import com.thoughtworks.xstream.XStream;
import cucumber.runtime.converters.DateConverter;
import cucumber.runtime.converters.LocalizedXStreams;
import cucumber.runtime.converters.SingleValueConverterWrapperExt;
import gherkin.I18n;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class ToDataTableTest {
    private TableConverter tc;

    @Before
    public void createTableConverterWithDateFormat() {
        XStream xStream = new LocalizedXStreams(Thread.currentThread().getContextClassLoader()).get(new I18n("en"));
        tc = new TableConverter(xStream);
        SingleValueConverterWrapperExt converterWrapper = (SingleValueConverterWrapperExt) xStream.getConverterLookup().lookupConverterForType(Date.class);
        DateConverter dateConverter = (DateConverter) converterWrapper.getConverter();
        dateConverter.setOnlyFormat("dd/MM/yyyy", Locale.UK);
    }

    @Test
    public void converts_list_of_beans_to_table() {
        List<UserPojo> users = tc.toList(UserPojo.class, personTable());
        DataTable table = tc.toTable(users);
        assertEquals("" +
                "      | name        | birthDate  | credits |\n" +
                "      | Sid Vicious | 10/05/1957 | 1,000   |\n" +
                "      | Frank Zappa | 21/12/1940 | 3,000   |\n" +
                "", table.toString());
    }

    private DataTable personTable() {
        return TableParser.parse("" +
                "| name        | birthDate  | credits  |\n" +
                "| Sid Vicious | 10/05/1957 | 1,000    |\n" +
                "| Frank Zappa | 21/12/1940 | 3,000    |\n" +
                "");
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
        public String name;
        public Date birthDate;
        public Integer credits;

        public UserPojo(int foo) {
        }
    }
}
