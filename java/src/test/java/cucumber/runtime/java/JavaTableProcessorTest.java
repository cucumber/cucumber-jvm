package cucumber.runtime.java;

import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.javabean.JavaBeanConverter;
import cucumber.annotation.DateFormat;
import cucumber.runtime.CucumberException;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.StepDefinitionMatch;
import cucumber.runtime.converters.LocalizedXStreams;
import cucumber.table.DataTable;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Comment;
import gherkin.formatter.model.DataTableRow;
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
        public List<UserPojo> listOfPojos;
        public List<UserBean> listOfBeans;
        public List<Double> listOfDoubles;
        public List<Map<String, String>> listOfMapsOfStringToString;
        public List<Map<String, Object>> listOfMapsOfStringToObject;

        public DataTable dataTable;

        public void listOfPojos(@DateFormat("yyyy-MM-dd") List<UserPojo> listOfPojos) {
            this.listOfPojos = listOfPojos;
        }

        public void listOfBeans(@DateFormat("yyyy-MM-dd") List<UserBean> listOfBeans) {
            this.listOfBeans = listOfBeans;
        }

        public void listOfDoubles(List<Double> listOfDoubles) {
            this.listOfDoubles = listOfDoubles;
        }

        public void listOfMapsOfStringToString(List<Map<String, String>> listOfMapsOfStringToString) {
            this.listOfMapsOfStringToString = listOfMapsOfStringToString;
        }

        public void listOfMapsOfStringToObject(List<Map<String, Object>> listOfMapsOfStringToObject) {
            this.listOfMapsOfStringToObject = listOfMapsOfStringToObject;
        }

        public void plainDataTable(DataTable dataTable) {
            this.dataTable = dataTable;
        }

        public void listOfMapsOfDateToString(List<Map<Date, String>> mapsOfDateToString) {
        }

        public void listOfMapsOfStringToDate(List<Map<String, Date>> mapsOfStringToDate) {
        }

        public void listOfMaps(List<Map> maps) {
        }
    }

    @Test
    public void transforms_to_list_of_pojos() throws Throwable {
        Method m = StepDefs.class.getMethod("listOfPojos", List.class);
        StepDefs stepDefs = runStepDef(m, listOfDatesWithHeader());
        assertEquals(sidsBirthday(), stepDefs.listOfPojos.get(0).birthDate);
    }

    @Test
    public void transforms_to_list_of_beans() throws Throwable {
        Method m = StepDefs.class.getMethod("listOfBeans", List.class);
        StepDefs stepDefs = runStepDef(m, listOfDatesWithHeader());
        assertEquals(sidsBirthday(), stepDefs.listOfBeans.get(0).getBirthDate());
    }

    @Test
    public void transforms_to_list_of_single_values() throws Throwable {
        Method m = StepDefs.class.getMethod("listOfDoubles", List.class);
        StepDefs stepDefs = runStepDef(m, listOfDoublesWithoutHeader());
        assertEquals("[[100.5, 99.5], [0.5, -0.5], [1000.0, 999.0]]", stepDefs.listOfDoubles.toString());
    }

    @Test
    public void transforms_to_list_of_map_of_string_to_string() throws Throwable {
        Method m = StepDefs.class.getMethod("listOfMapsOfStringToString", List.class);
        StepDefs stepDefs = runStepDef(m, listOfDatesWithHeader());
        assertEquals("1957-05-10", stepDefs.listOfMapsOfStringToString.get(0).get("Birth Date"));
    }

    @Test
    public void transforms_to_list_of_map_of_string_to_object() throws Throwable {
        Method m = StepDefs.class.getMethod("listOfMapsOfStringToObject", List.class);
        StepDefs stepDefs = runStepDef(m, listOfDatesWithHeader());
        assertEquals("1957-05-10", stepDefs.listOfMapsOfStringToObject.get(0).get("Birth Date"));
    }

    @Test
    public void passes_plain_data_table() throws Throwable {
        Method m = StepDefs.class.getMethod("plainDataTable", DataTable.class);
        StepDefs stepDefs = runStepDef(m, listOfDatesWithHeader());
        assertEquals("1957-05-10", stepDefs.dataTable.raw().get(1).get(0));
        assertEquals("Birth Date", stepDefs.dataTable.raw().get(0).get(0));
    }

    @Test
    public void does_not_transform_to_list_of_map_of_date_to_string() throws Throwable {
        thrown.expect(CucumberException.class);
        thrown.expectMessage("Tables can only be transformed to a List<Map<K,V>> when K is String. It was class java.util.Date.");

        Method listOfBeans = StepDefs.class.getMethod("listOfMapsOfDateToString", List.class);
        runStepDef(listOfBeans, listOfDatesWithHeader());
    }

    @Test
    public void does_not_transform_to_list_of_map_of_string_to_date() throws Throwable {
        thrown.expect(CucumberException.class);
        thrown.expectMessage("Tables can only be transformed to a List<Map<K,V>> when V is String or Object. It was class java.util.Date.");

        Method listOfBeans = StepDefs.class.getMethod("listOfMapsOfStringToDate", List.class);
        runStepDef(listOfBeans, listOfDatesWithHeader());
    }

    @Test
    public void does_not_transform_to_list_of_non_generic_map() throws Throwable {
        thrown.expect(CucumberException.class);
        thrown.expectMessage("Tables can only be transformed to List<Map<String,String>> or List<Map<String,Object>>. You have to declare generic types.");

        Method listOfBeans = StepDefs.class.getMethod("listOfMaps", List.class);
        runStepDef(listOfBeans, listOfDatesWithHeader());
    }

    private StepDefs runStepDef(Method method, List<DataTableRow> rows) throws Throwable {
        StepDefs stepDefs = new StepDefs();
        StepDefinition stepDefinition = new JavaStepDefinition(method, Pattern.compile("whatever"), new SingletonFactory(stepDefs));

        Step stepWithRows = new Step(NO_COMMENTS, "Given ", "something", 10, rows, null);

        StepDefinitionMatch stepDefinitionMatch = new StepDefinitionMatch(NO_ARGS, stepDefinition, "some.feature", stepWithRows, new LocalizedXStreams());
        stepDefinitionMatch.runStep(Locale.UK);
        return stepDefs;
    }

    private List<DataTableRow> listOfDatesWithHeader() {
        List<DataTableRow> rows = new ArrayList<DataTableRow>();
        rows.add(new DataTableRow(NO_COMMENTS, asList("Birth Date"), 1));
        rows.add(new DataTableRow(NO_COMMENTS, asList("1957-05-10"), 2));
        return rows;
    }

    private List<DataTableRow> listOfDoublesWithoutHeader() {
        List<DataTableRow> rows = new ArrayList<DataTableRow>();
        rows.add(new DataTableRow(NO_COMMENTS, asList("100.5", "99.5"), 2));
        rows.add(new DataTableRow(NO_COMMENTS, asList("0.5", "-0.5"), 2));
        rows.add(new DataTableRow(NO_COMMENTS, asList("1000", "999"), 2));
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
