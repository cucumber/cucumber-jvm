package cucumber.runtime.java;

import cucumber.runtime.StepDefinition;
import cucumber.runtime.StepDefinitionMatch;
import cucumber.runtime.converters.LocalizedXStreams;
import cucumber.table.java.User;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Comment;
import gherkin.formatter.model.Row;
import gherkin.formatter.model.Step;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static junit.framework.Assert.assertEquals;

public class JavaBeanTableProcessorTest {

    private static final List<Argument> NO_ARGS = emptyList();
    private static final List<Comment> NO_COMMENTS = emptyList();

    public static class StepDefs {
        public List<User> users;

        public void stepMethodWithList(List<User> users) {
            this.users = users;
        }
    }

    @Test
    public void shouldExecuteWithAListOfUsers() throws Throwable {
        StepDefs stepDefs = new StepDefs();
        StepDefinition stepDefinition = new JavaStepDefinition(Pattern.compile("whatever"), stepMethodWithList(), new SingletonFactory(stepDefs));

        Step stepWithRows = new Step(NO_COMMENTS, "Given", "something that wants users", 10);
        stepWithRows.setMultilineArg(rowsList());

        StepDefinitionMatch stepDefinitionMatch = new StepDefinitionMatch(NO_ARGS, stepDefinition, "some.feature", stepWithRows, new LocalizedXStreams());
        stepDefinitionMatch.runStep(Locale.UK);

        assertEquals(asList(new User("Sid Vicious", sidsBirthday(), 1000)), stepDefs.users);
    }

    private Method stepMethodWithList() throws SecurityException, NoSuchMethodException {
        return StepDefs.class.getMethod("stepMethodWithList", List.class);
    }

    private List<Row> rowsList() {
        List<Row> rows = new ArrayList<Row>();
        rows.add(new Row(new ArrayList<Comment>(), asList("name", "birth date", "credits"), 1));
        rows.add(new Row(new ArrayList<Comment>(), asList("Sid Vicious", "10/05/1957", "1000"), 2));
        return rows;
    }

    private Date sidsBirthday() {
        Calendar sidsBirthDay = Calendar.getInstance();
        sidsBirthDay.set(1957, 4, 10, 0, 0, 0);
        sidsBirthDay.set(Calendar.MILLISECOND, 0);
        sidsBirthDay.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sidsBirthDay.getTime();
    }
}
