package cucumber.table.java;

import gherkin.formatter.model.Comment;
import gherkin.formatter.model.Row;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import cucumber.table.Table;

public class JavaBeanTableTransformerTest {

    private Table userTable;

    @Before
    public void initSimpleTable() {
        List<Row> rows = new ArrayList<Row>();
        rows.add(new Row(new ArrayList<Comment>(), Arrays.asList("name", "birth date", "credits"), 1));
        rows.add(new Row(new ArrayList<Comment>(), Arrays.asList("Sid Vicious", "5/10/1957", "1000"), 2));
        this.userTable = new Table(rows, Locale.ENGLISH);
    }

    @Test
    public void shouldTransformToUser() {
        List<User> users = new JavaBeanTableTransformer(User.class).transformTable(this.userTable);
        Calendar instance = Calendar.getInstance();
        instance.set(1957, 4, 10, 0, 0, 0);
        instance.set(Calendar.MILLISECOND, 0);
        User expectedUser = new User("Sid Vicious", instance.getTime(), 1000);
        Assert.assertEquals("Number of objects", 1, users.size());
        Assert.assertEquals("User", expectedUser, users.get(0));
    }
}
