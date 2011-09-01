package cucumber.table;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static java.util.Arrays.asList;

public class TableConverterTest {

    @Test
    public void converts_table_to_list_of_pojos() {
        TableConverter tc = new TableConverter();
        List<User> users = tc.convert(User.class, headerRow(), bodyRows());
        System.out.println("users = " + users);
    }

    private List<String> headerRow() {
        return asList("name", "birthDate", "credits");
    }

    private List<List<String>> bodyRows() {
        return asList(
                asList("Sid Vicious", "1957-10-05 12:30:39 UTC", "1000"),
                asList("Frank Zappa", "1940-12-21 14:56:12 UTC", "3000")
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
