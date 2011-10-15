package cucumber.table;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.javabean.JavaBeanConverter;
import cucumber.runtime.converters.LocalizedXStreams;
import org.junit.Test;

import java.util.*;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class TableConverterTest {

    @Test
    public void converts_table_to_list_of_pojos() {
        XStream xStream = new LocalizedXStreams().get(Locale.UK);
        TableConverter tc = new TableConverter(xStream);
        List<UserPojo> users = tc.convert(UserPojo.class, headerRow(), bodyRows());
        assertEquals(sidsBirthday(), users.get(0).birthDate);
    }

    @Test
    public void converts_table_to_list_of_beans() {
        XStream xStream = new LocalizedXStreams().get(Locale.UK);
        TableConverter tc = new TableConverter(xStream);
        List<UserBean> users = tc.convert(UserBean.class, headerRow(), bodyRows());
        assertEquals(sidsBirthday(), users.get(0).getBirthDate());
    }

    @Test
    public void converts_table_to_map_of_string_string() {
        XStream xStream = new LocalizedXStreams().get(Locale.UK);
        TableConverter tc = new TableConverter(xStream);

        List<Map<String, String>> users = tc.convert(Map.class, headerRow(), bodyRows());
        assertEquals("10/05/1957", users.get(0).get("birthDate"));
    }

    private List<String> headerRow() {
        return asList("name", "birthDate", "credits");
    }

    private List<List<String>> bodyRows() {
        return asList(
                asList("Sid Vicious", "10/05/1957", "1000"),
                asList("Frank Zappa", "21/12/1940", "3000")
        );
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
}
