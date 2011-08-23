package cucumber.table.java;

import gherkin.formatter.model.Comment;
import gherkin.formatter.model.Row;

import java.util.*;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import cucumber.table.Table;

import static junit.framework.Assert.assertEquals;

public class JavaBeanTableTransformerTest {

    private Table userTable;

    @Before
    public void initSimpleTable() {
        List<Row> rows = new ArrayList<Row>();
        rows.add(new Row(new ArrayList<Comment>(), Arrays.asList("name", "birth date", "credits"), 1));
        rows.add(new Row(new ArrayList<Comment>(), Arrays.asList("Sid Vicious", "5/10/1957", "1000"), 2));
        userTable = new Table(rows, Locale.ENGLISH);
    }

    @Test
    public void shouldTransformToUser() {
        List<User> users = new JavaBeanTableTransformer(User.class).transformTable(this.userTable);
        User expectedUser = new User("Sid Vicious", sidsBirthday(), 1000);
        assertEquals("Number of objects", 1, users.size());
        assertEquals("User", expectedUser, users.get(0));
    }

    private Date sidsBirthday() {
        Calendar sidsBirthDay = Calendar.getInstance();
        sidsBirthDay.set(1957, 4, 10, 0, 0, 0);
        sidsBirthDay.set(Calendar.MILLISECOND, 0);
        return sidsBirthDay.getTime();
    }
}
