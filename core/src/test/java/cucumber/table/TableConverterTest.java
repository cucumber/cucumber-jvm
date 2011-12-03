package cucumber.table;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.javabean.JavaBeanConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import cucumber.runtime.converters.DateConverter;
import cucumber.runtime.converters.LocalizedXStreams;
import cucumber.runtime.converters.SingleValueConverterWrapperExt;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.*;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class TableConverterTest {
    TableConverter tc;

    @Before
    public void createTableConverterWithDateFormat() {
        XStream xStream = new LocalizedXStreams().get(Locale.UK);
        tc = new TableConverter(xStream);
        SingleValueConverterWrapperExt converterWrapper = (SingleValueConverterWrapperExt) xStream.getConverterLookup().lookupConverterForType(Date.class);
        DateConverter dateConverter = (DateConverter) converterWrapper.getConverter();
        dateConverter.setOnlyFormat("dd/MM/yyyy", Locale.UK);
    }

    @Test
    public void converts_table_to_list_of_pojos() {
        List<UserPojo> users = tc.toList(UserPojo.class, personTable());
        assertEquals(sidsBirthday(), users.get(0).birthDate);

        DataTable table = tc.toTable(users);
        assertEquals(personTable().toString(), table.toString());
    }

    @Test
    public void converts_table_to_list_of_beans() {
        List<UserBean> users = tc.toList(UserBean.class, personTable());
        assertEquals(sidsBirthday(), users.get(0).getBirthDate());
    }

    @Test
    public void converts_table_to_list_of_class_with_special_fields() {
        List<UserWithNameField> users = tc.toList(UserWithNameField.class, personTable());
        assertEquals("Sid", users.get(0).name.first);
        assertEquals("Vicious", users.get(0).name.last);
    }

    @Test
    public void converts_table_to_map_of_string_string() {
        Type mapType = new TypeReference<Map<String, String>>() {
        }.getType();
        List<Map<String, String>> users = tc.toList(mapType, personTable());
        assertEquals("10/05/1957", users.get(0).get("birthDate"));
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
        Type listOfDoubleType = new TypeReference<List<Double>>(){}.getType();
        List<List<Double>> actual = tc.toList(listOfDoubleType, table);
        assertEquals(lists, actual);
    }

    private Date sidsBirthday() {
        Calendar sidsBirthDay = Calendar.getInstance();
        sidsBirthDay.setTimeZone(TimeZone.getTimeZone("UTC"));
        sidsBirthDay.set(1957, 4, 10, 0, 0, 0);
        sidsBirthDay.set(Calendar.MILLISECOND, 0);
        return sidsBirthDay.getTime();
    }

    // No setters
    public static class UserPojo {
        public String name;
        public Date birthDate;
        public Integer credits;

        public UserPojo(int foo) {
        }
    }

    @XStreamConverter(JavaBeanConverter.class)
    public static class UserBean {
        private String nameX;
        private Date birthDateX;
        private Integer creditsX;

        public String getName() {
            return nameX;
        }

        public void setName(String name) {
            this.nameX = name;
        }

        public Date getBirthDate() {
            return birthDateX;
        }

        public void setBirthDate(Date birthDate) {
            this.birthDateX = birthDate;
        }

        public Integer getCredits() {
            return creditsX;
        }

        public void setCredits(Integer credits) {
            this.creditsX = credits;
        }
    }

    public static class UserWithNameField {
        public Name name;
        public Date birthDate;
        public Integer credits;
    }

    @XStreamConverter(NameConverter.class)
    public static class Name {
        public String first;
        public String last;
    }

    public static class NameConverter implements Converter {
        @Override
        public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
            Name name = new Name();
            String[] firstLast = reader.getValue().split(" ");
            name.first = firstLast[0];
            name.last = firstLast[1];
            return name;
        }

        @Override
        public boolean canConvert(Class type) {
            return type.equals(Name.class);
        }
    }
}
