package cucumber.runtime.java;

import gherkin.formatter.Argument;
import gherkin.formatter.model.Comment;
import gherkin.formatter.model.Row;
import gherkin.formatter.model.Step;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.junit.Test;

import static org.mockito.Mockito.when;

import static org.mockito.Mockito.mock;

import cucumber.annotation.JavaBeanClass;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.StepDefinitionMatch;
import cucumber.runtime.TableArgumentProcessor;
import cucumber.runtime.transformers.Transformers;
import cucumber.table.java.User;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class JavaBeanTableProcessorTest {

    @Test
    public void shouldReturnAJavaBeanProcessorWithUser() throws Throwable {
        StepDefinition stepDefinition = new JavaStepDefinition(Pattern.compile("^.*$"), stepMethodWithList(),
                mock(ObjectFactory.class));
        TableArgumentProcessor tableProcessor = stepDefinition.getTableProcessor(0);
        assertNotNull("TableArgumentProcessor is null", tableProcessor);
        assertTrue("TableArgumentProcessor is not a JavaBeanTableProcessor",
                tableProcessor instanceof JavaBeanTableProcessor);
        assertEquals("JavaBeanTableProcessor wasn't initialized with User class", User.class,
                ((JavaBeanTableProcessor) tableProcessor).getBeanClass());
    }

    @Test
    public void shouldExecuteWithAListOfUsers() throws Throwable {
        List<Argument> arguments = Arrays.asList(new Argument(0, ""));
        StepDefinition stepDefinition = mock(JavaStepDefinition.class);
        when(stepDefinition.getParameterTypes()).thenReturn(new Class<?>[] { List.class });
        when(stepDefinition.getTableProcessor(0)).thenReturn(new JavaBeanTableProcessor(User.class));
        Step stepWithRows = mock(Step.class);
        when(stepWithRows.getDocString()).thenReturn(null);
        when(stepWithRows.getRows()).thenReturn(rowsList());
        StepDefinitionMatch stepDefinitionMatch = new StepDefinitionMatch(arguments, stepDefinition, stepWithRows,
                new Transformers());
        stepDefinitionMatch.runStep(stepWithRows, "step-definition-match-test", Locale.ENGLISH);
        Object[] args = { Arrays.asList(new User("Sid Vicious", sidsBirthday(), 1000)) };
        verify(stepDefinition).execute(args);
    }

    private Method stepMethodWithList() throws SecurityException, NoSuchMethodException {
        return getClass().getMethod("stepMethodWithList", List.class);
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

    public void stepMethodWithList(@JavaBeanClass(User.class) List<User> users) {
        //
    }
}
