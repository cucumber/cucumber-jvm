package cucumber.runtime.java;

import cucumber.annotation.JavaBeanClass;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.StepDefinitionMatch;
import cucumber.runtime.transformers.Transformers;
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
import static junit.framework.Assert.assertEquals;

public class JavaBeanTableProcessorTest {

    public static class StepDefs {
        public List<User> users;

        public void stepMethodWithList(@JavaBeanClass(User.class) List<User> users) {
            this.users = users;
        }
    }

    @Test
    public void shouldExecuteWithAListOfUsers() throws Throwable {
        StepDefs stepDefs = new StepDefs();
        StepDefinition stepDefinition = new JavaStepDefinition(Pattern.compile("whatever"), stepMethodWithList(), new SingletonFactory(stepDefs));

        Step stepWithRows = new Step(Collections.<Comment>emptyList(), "Given", "something that wants users", 10);
        stepWithRows.setMultilineArg(rowsList());

        StepDefinitionMatch stepDefinitionMatch = new StepDefinitionMatch(Collections.<Argument>emptyList(), stepDefinition, "some.feature", stepWithRows, new Transformers());
        stepDefinitionMatch.runStep(Locale.ENGLISH);

        assertEquals(asList(new User("Sid Vicious", sidsBirthday(), 1000)), stepDefs.users);
    }

    private Method stepMethodWithList() throws SecurityException, NoSuchMethodException {
        return StepDefs.class.getMethod("stepMethodWithList", List.class);
    }

    private List<Row> rowsList() {
        List<Row> rows = new ArrayList<Row>();
        rows.add(new Row(new ArrayList<Comment>(), Arrays.asList("name", "birth date", "credits"), 1));
        rows.add(new Row(new ArrayList<Comment>(), Arrays.asList("Sid Vicious", "5/10/1957", "1000"), 2));
        return rows;
    }

    private Date sidsBirthday() {
        Calendar sidsBirthDay = Calendar.getInstance();
        sidsBirthDay.set(1957, 4, 10, 0, 0, 0);
        sidsBirthDay.set(Calendar.MILLISECOND, 0);
        return sidsBirthDay.getTime();
    }
}
