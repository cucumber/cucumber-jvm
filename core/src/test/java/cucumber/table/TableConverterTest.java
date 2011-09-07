package cucumber.table;

import com.thoughtworks.xstream.XStream;
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
        List<User> users = tc.convert(User.class, headerRow(), bodyRows());
        assertEquals(sidsBirthday(), users.get(0).birthDate);
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

    public static class User {
        public String name;
        public Date birthDate;
        public Integer credits;

        @Override
        public String toString() {
            return "User{" +
                    "name='" + name + '\'' +
                    ", birthDate=" + birthDate +
                    ", credits=" + credits +
                    '}';
        }
    }
}
