package cucumber.runtime.java;

import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.javabean.JavaBeanConverter;
import cucumber.runtime.CucumberException;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.StepDefinitionMatch;
import cucumber.runtime.converters.LocalizedXStreams;
import cucumber.table.CamelCaseHeaderMapper;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Comment;
import gherkin.formatter.model.Row;
import gherkin.formatter.model.Step;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static junit.framework.Assert.assertEquals;

public class JavaTableProcessorTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private static final List<Argument> NO_ARGS = emptyList();
    private static final List<Comment> NO_COMMENTS = emptyList();

    public static class StepDefs {
        public List<UserPojo> userPojos;
        public List<UserBean> userBeans;
        public List<Map<String, String>> mapsOfStringToString;
        public List<Map<String, Object>> mapsOfStringToObject;
        public List<Map<Date, String>> mapsOfDateToString;
        public List<Map<String, Date>> mapsOfStringToDate;

        public void listOfPojos(List<UserPojo> userPojos) {
            this.userPojos = userPojos;
        }

        public void listOfBeans(List<UserBean> userBeans) {
            this.userBeans = userBeans;
        }

        public void listOfMapsOfStringToString(List<Map<String, String>> userMaps) {
            this.mapsOfStringToString = userMaps;
        }

        public void listOfMapsOfStringToObject(List<Map<String, Object>> mapsOfStringToObject) {
            this.mapsOfStringToObject = mapsOfStringToObject;
        }

        public void listOfMapsOfDateToString(List<Map<Date, String>> mapsOfDateToString) {
            this.mapsOfDateToString = mapsOfDateToString;
        }

        public void listOfMapsOfStringToDate(List<Map<String, Date>> mapsOfStringToDate) {
            this.mapsOfStringToDate = mapsOfStringToDate;
        }
    }

    @Test
    public void transforms_to_list_of_pojos() throws Throwable {
        Method listOfPojos = StepDefs.class.getMethod("listOfPojos", List.class);
        StepDefs stepDefs = runStepDef(listOfPojos);
        assertEquals(sidsBirthday(), stepDefs.userPojos.get(0).birthDate);
    }

    @Test
    public void transforms_to_list_of_beans() throws Throwable {
        Method listOfBeans = StepDefs.class.getMethod("listOfBeans", List.class);
        StepDefs stepDefs = runStepDef(listOfBeans);
        assertEquals(sidsBirthday(), stepDefs.userBeans.get(0).getBirthDate());
    }

    @Test
    public void transforms_to_list_of_map_of_string_to_string() throws Throwable {
        Method listOfBeans = StepDefs.class.getMethod("listOfMapsOfStringToString", List.class);
        StepDefs stepDefs = runStepDef(listOfBeans);
        assertEquals("10/05/1957", stepDefs.mapsOfStringToString.get(0).get("birthDate"));
    }

    @Test
    public void transforms_to_list_of_map_of_string_to_object() throws Throwable {
        Method listOfBeans = StepDefs.class.getMethod("listOfMapsOfStringToObject", List.class);
        StepDefs stepDefs = runStepDef(listOfBeans);
        assertEquals("10/05/1957", stepDefs.mapsOfStringToObject.get(0).get("birthDate"));
    }

    @Test
    public void does_not_transform_to_list_of_map_of_date_to_string() throws Throwable {
        thrown.expect(CucumberException.class);
        thrown.expectMessage("Tables can only be transformed to a List<Map<K,V>> when K is String. It was class java.util.Date");
        
        Method listOfBeans = StepDefs.class.getMethod("listOfMapsOfDateToString", List.class);
        StepDefs stepDefs = runStepDef(listOfBeans);
        assertEquals("10/05/1957", stepDefs.mapsOfDateToString.get(0).get("birthDate"));
    }

    @Test
    public void does_not_transform_to_list_of_map_of_string_to_date() throws Throwable {
        thrown.expect(CucumberException.class);
        thrown.expectMessage("Tables can only be transformed to a List<Map<K,V>> when V is String or Object. It was class java.util.Date");
        
        Method listOfBeans = StepDefs.class.getMethod("listOfMapsOfStringToDate", List.class);
        StepDefs stepDefs = runStepDef(listOfBeans);
        assertEquals("10/05/1957", stepDefs.mapsOfStringToDate.get(0).get("birthDate"));
    }

    private StepDefs runStepDef(Method method) throws Throwable {
        StepDefs stepDefs = new StepDefs();
        StepDefinition stepDefinition = new JavaStepDefinition(Pattern.compile("whatever"), method, new SingletonFactory(stepDefs));

        Step stepWithRows = new Step(NO_COMMENTS, "Given ", "something that wants users", 10);
        stepWithRows.setRows(rowsList());

        StepDefinitionMatch stepDefinitionMatch = new StepDefinitionMatch(NO_ARGS, stepDefinition, "some.feature", stepWithRows, new LocalizedXStreams(), new CamelCaseHeaderMapper());
        stepDefinitionMatch.runStep(Locale.UK);
        return stepDefs;
    }

    private List<Row> rowsList() {
        List<Row> rows = new ArrayList<Row>();
        rows.add(new Row(NO_COMMENTS, asList("birth date"), 1));
        rows.add(new Row(NO_COMMENTS, asList("10/05/1957"), 2));
        return rows;
    }

    private Date sidsBirthday() {
        Calendar sidsBirthDay = Calendar.getInstance();
        sidsBirthDay.set(1957, 4, 10, 0, 0, 0);
        sidsBirthDay.set(Calendar.MILLISECOND, 0);
        sidsBirthDay.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sidsBirthDay.getTime();
    }

    public static class UserPojo {
        private Date birthDate;
    }

    @XStreamConverter(JavaBeanConverter.class)
    public static class UserBean {
        private Date birthDateX;

        public Date getBirthDate() {
            return this.birthDateX;
        }

        public void setBirthDate(Date birthDate) {
            this.birthDateX = birthDate;
        }
    }
}
