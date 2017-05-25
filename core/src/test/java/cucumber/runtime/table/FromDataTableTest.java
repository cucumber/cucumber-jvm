package cucumber.runtime.table;

import cucumber.api.DataTable;
import cucumber.api.Format;
import cucumber.api.Transformer;
import cucumber.api.Transpose;
import cucumber.deps.com.thoughtworks.xstream.annotations.XStreamConverter;
import cucumber.deps.com.thoughtworks.xstream.converters.javabean.JavaBeanConverter;
import cucumber.runtime.Argument;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.StepDefinitionMatch;
import cucumber.runtime.StubStepDefinition;
import cucumber.runtime.xstream.LocalizedXStreams;
import gherkin.pickles.PickleCell;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleRow;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleTable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

public class FromDataTableTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private static final List<Argument> NO_ARGS = emptyList();
    private static final String ENGLISH = "en";

    public static class StepDefs {
        public List<PrimitiveContainer> listOfPrimitiveContainers;
        public List<UserPojo> listOfPojos;
        public List<UserBean> listOfBeans;
        public List<UserWithNameField> listOfUsersWithNameField;
        public List<List<Double>> listOfListOfDoubles;
        public List<Map<String, Date>> listOfMapsOfStringToDate;
        public List<Map<String, Object>> listOfMapsOfStringToObject;
        public Map<Double, Double> mapOfDoubleToDouble;

        public DataTable dataTable;

        public void listOfPrimitiveContainers(List<PrimitiveContainer> primitiveContainers) {
            this.listOfPrimitiveContainers = primitiveContainers;
        }

        public void listOfPojos(@Format("yyyy-MM-dd") List<UserPojo> listOfPojos) {
            this.listOfPojos = listOfPojos;
        }

        public void listOfPojosTransposed(@Transpose @Format("yyyy-MM-dd") List<UserPojo> listOfPojos) {
            this.listOfPojos = listOfPojos;
        }

        public void listOfBeans(@Format("yyyy-MM-dd") List<UserBean> listOfBeans) {
            this.listOfBeans = listOfBeans;
        }

        public void listOfBeansTransposed(@Transpose @Format("yyyy-MM-dd") List<UserBean> listOfBeans) {
            this.listOfBeans = listOfBeans;
        }

        public void listOfUsersWithNameField(@Format("yyyy-MM-dd") List<UserWithNameField> listOfUsersWithNameField) {
            this.listOfUsersWithNameField = listOfUsersWithNameField;
        }

        public void listOfUsersTransposedWithNameField(@Transpose @Format("yyyy-MM-dd") List<UserWithNameField> listOfUsersWithNameField) {
            this.listOfUsersWithNameField = listOfUsersWithNameField;
        }

        public void listOfListOfDoubles(List<List<Double>> listOfListOfDoubles) {
            this.listOfListOfDoubles = listOfListOfDoubles;
        }

        public void listOfListOfDoublesTransposed(@Transpose List<List<Double>> listOfListOfDoubles) {
            this.listOfListOfDoubles = listOfListOfDoubles;
        }

        public void listOfMapsOfStringToDate(@Format("yyyy-MM-dd") List<Map<String, Date>> listOfMapsOfStringToDate) {
            this.listOfMapsOfStringToDate = listOfMapsOfStringToDate;
        }

        public void listOfMapsOfStringToObject(List<Map<String, Object>> listOfMapsOfStringToObject) {
            this.listOfMapsOfStringToObject = listOfMapsOfStringToObject;
        }

        public void plainDataTable(DataTable dataTable) {
            this.dataTable = dataTable;
        }

        public void listOfMapsOfDateToString(List<Map<Date, String>> mapsOfDateToString) {
        }

        public void listOfMaps(List<Map> maps) {
        }

        public void mapOfDoubleToDouble(Map<Double,Double> mapOfDoubleToDouble) {
            this.mapOfDoubleToDouble = mapOfDoubleToDouble;
        }
    }

    @Test
    public void transforms_to_list_of_pojos() throws Throwable {
        Method m = StepDefs.class.getMethod("listOfPojos", List.class);
        StepDefs stepDefs = runStepDef(m, new PickleTable(listOfDatesAndCalWithHeader()));
        assertEquals(sidsBirthday(), stepDefs.listOfPojos.get(0).birthDate);
        assertEquals(sidsDeathcal().getTime(), stepDefs.listOfPojos.get(0).deathCal.getTime());
        assertNull(stepDefs.listOfPojos.get(1).deathCal);
    }

    @Test
    public void transforms_to_list_of_pojos_transposed() throws Throwable {
        Method m = StepDefs.class.getMethod("listOfPojosTransposed", List.class);
        StepDefs stepDefs = runStepDef(m, new PickleTable(transposedListOfDatesAndCalWithHeader()));
        assertEquals(sidsBirthday(), stepDefs.listOfPojos.get(0).birthDate);
        assertEquals(sidsDeathcal().getTime(), stepDefs.listOfPojos.get(0).deathCal.getTime());
        assertNull(stepDefs.listOfPojos.get(1).deathCal);
    }

    @Test
    public void assigns_null_to_objects_when_empty_except_boolean_special_case() throws Throwable {
        Method m = StepDefs.class.getMethod("listOfPrimitiveContainers", List.class);

        List<PickleRow> rows = new ArrayList<PickleRow>();
        rows.add(new PickleRow(asList(new PickleCell(mock(PickleLocation.class), "number"), new PickleCell(mock(PickleLocation.class), "bool"), new PickleCell(mock(PickleLocation.class), "bool2"))));
        rows.add(new PickleRow(asList(new PickleCell(mock(PickleLocation.class), "1"), new PickleCell(mock(PickleLocation.class), "false"), new PickleCell(mock(PickleLocation.class), "true"))));
        rows.add(new PickleRow(asList(new PickleCell(mock(PickleLocation.class), ""), new PickleCell(mock(PickleLocation.class), ""), new PickleCell(mock(PickleLocation.class), ""))));
        PickleTable table = new PickleTable(rows);

        StepDefs stepDefs = runStepDef(m, table);

        assertEquals(new Integer(1), stepDefs.listOfPrimitiveContainers.get(0).number);
        assertEquals(new Boolean(false), stepDefs.listOfPrimitiveContainers.get(0).bool);
        assertEquals(true, stepDefs.listOfPrimitiveContainers.get(0).bool2);

        assertEquals(null, stepDefs.listOfPrimitiveContainers.get(1).number);
        assertEquals(new Boolean(false), stepDefs.listOfPrimitiveContainers.get(1).bool);
        assertEquals(false, stepDefs.listOfPrimitiveContainers.get(1).bool2);
    }

    @Test
    public void transforms_to_list_of_beans() throws Throwable {
        Method m = StepDefs.class.getMethod("listOfBeans", List.class);
        StepDefs stepDefs = runStepDef(m, new PickleTable(listOfDatesWithHeader()));
        assertEquals(sidsBirthday(), stepDefs.listOfBeans.get(0).getBirthDate());
    }

    @Test
    public void transforms_to_list_of_beans_transposed() throws Throwable {
        Method m = StepDefs.class.getMethod("listOfBeansTransposed", List.class);
        StepDefs stepDefs = runStepDef(m, new PickleTable(transposedListOfDatesWithHeader()));
        assertEquals(sidsBirthday(), stepDefs.listOfBeans.get(0).getBirthDate());
    }

    @Test
    public void converts_table_to_list_of_class_with_special_fields() throws Throwable {
        Method m = StepDefs.class.getMethod("listOfUsersWithNameField", List.class);
        StepDefs stepDefs = runStepDef(m, new PickleTable(listOfDatesAndNamesWithHeader()));
        assertEquals(sidsBirthday(), stepDefs.listOfUsersWithNameField.get(0).birthDate);
        assertEquals("Sid", stepDefs.listOfUsersWithNameField.get(0).name.first);
        assertEquals("Vicious", stepDefs.listOfUsersWithNameField.get(0).name.last);
    }

    @Test
    public void converts_table_to_list_of_class_with_special_fields_transposed() throws Throwable {
        Method m = StepDefs.class.getMethod("listOfUsersTransposedWithNameField", List.class);
        StepDefs stepDefs = runStepDef(m, new PickleTable(transposedListOfDatesAndNamesWithHeader()));
        assertEquals(sidsBirthday(), stepDefs.listOfUsersWithNameField.get(0).birthDate);
        assertEquals("Sid", stepDefs.listOfUsersWithNameField.get(0).name.first);
        assertEquals("Vicious", stepDefs.listOfUsersWithNameField.get(0).name.last);
    }

    @Test
    public void transforms_to_map_of_double_to_double() throws Throwable {
        Method m = StepDefs.class.getMethod("mapOfDoubleToDouble", Map.class);
        StepDefs stepDefs = runStepDef(m, new PickleTable(listOfDoublesWithoutHeader()));
        assertEquals(Double.valueOf(999.0), stepDefs.mapOfDoubleToDouble.get(1000.0));
        assertEquals(Double.valueOf(-0.5), stepDefs.mapOfDoubleToDouble.get(0.5));
        assertEquals(Double.valueOf(99.5), stepDefs.mapOfDoubleToDouble.get(100.5));
    }

    @Test
    public void transforms_to_list_of_single_values() throws Throwable {
        Method m = StepDefs.class.getMethod("listOfListOfDoubles", List.class);
        StepDefs stepDefs = runStepDef(m, new PickleTable(listOfDoublesWithoutHeader()));
        assertEquals("[[100.5, 99.5], [0.5, -0.5], [1000.0, 999.0]]", stepDefs.listOfListOfDoubles.toString());
    }

    @Test
    public void transforms_to_list_of_single_values_transposed() throws Throwable {
        Method m = StepDefs.class.getMethod("listOfListOfDoublesTransposed", List.class);
        StepDefs stepDefs = runStepDef(m, new PickleTable(transposedListOfDoublesWithoutHeader()));
        assertEquals("[[100.5, 99.5], [0.5, -0.5], [1000.0, 999.0]]", stepDefs.listOfListOfDoubles.toString());
    }

    @Test
    public void transforms_to_list_of_map_of_string_to_date() throws Throwable {
        Method m = StepDefs.class.getMethod("listOfMapsOfStringToDate", List.class);
        StepDefs stepDefs = runStepDef(m, new PickleTable(listOfDatesWithHeader()));
        assertEquals(sidsBirthday(), stepDefs.listOfMapsOfStringToDate.get(0).get("Birth Date"));
    }

    @Test
    public void transforms_to_list_of_map_of_string_to_object() throws Throwable {
        Method m = StepDefs.class.getMethod("listOfMapsOfStringToObject", List.class);
        StepDefs stepDefs = runStepDef(m, new PickleTable(listOfDatesWithHeader()));
        assertEquals("1957-05-10", stepDefs.listOfMapsOfStringToObject.get(0).get("Birth Date"));
    }

    @Test
    public void passes_plain_data_table() throws Throwable {
        Method m = StepDefs.class.getMethod("plainDataTable", DataTable.class);
        StepDefs stepDefs = runStepDef(m, new PickleTable(listOfDatesWithHeader()));
        assertEquals("1957-05-10", stepDefs.dataTable.raw().get(1).get(0));
        assertEquals("Birth Date", stepDefs.dataTable.raw().get(0).get(0));
    }

    private StepDefs runStepDef(Method method, PickleTable table) throws Throwable {
        StepDefs stepDefs = new StepDefs();
        StepDefinition stepDefinition = new StubStepDefinition(stepDefs, method, "some pattern");

        PickleStep stepWithTable = new PickleStep("something", asList((gherkin.pickles.Argument)table), asList(mock(PickleLocation.class)));

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        StepDefinitionMatch stepDefinitionMatch = new StepDefinitionMatch(NO_ARGS, stepDefinition, "some.feature", stepWithTable, new LocalizedXStreams(classLoader));
        stepDefinitionMatch.runStep(ENGLISH, null);
        return stepDefs;
    }

    private List<PickleRow> listOfDatesWithHeader() {
        List<PickleRow> rows = new ArrayList<PickleRow>();
        rows.add(new PickleRow(asList(new PickleCell(mock(PickleLocation.class), "Birth Date"))));
        rows.add(new PickleRow(asList(new PickleCell(mock(PickleLocation.class), "1957-05-10"))));
        return rows;
    }

    private List<PickleRow> listOfDatesAndCalWithHeader() {
        List<PickleRow> rows = new ArrayList<PickleRow>();
        rows.add(new PickleRow(asList(new PickleCell(mock(PickleLocation.class), "Birth Date"), new PickleCell(mock(PickleLocation.class), "Death Cal"))));
        rows.add(new PickleRow(asList(new PickleCell(mock(PickleLocation.class), "1957-05-10"), new PickleCell(mock(PickleLocation.class), "1979-02-02"))));
        rows.add(new PickleRow(asList(new PickleCell(mock(PickleLocation.class), ""), new PickleCell(mock(PickleLocation.class), ""))));
        return rows;
    }

    private List<PickleRow> listOfDatesAndNamesWithHeader() {
        List<PickleRow> rows = new ArrayList<PickleRow>();
        rows.add(new PickleRow(asList(new PickleCell(mock(PickleLocation.class), "Birth Date"), new PickleCell(mock(PickleLocation.class), "Name"))));
        rows.add(new PickleRow(asList(new PickleCell(mock(PickleLocation.class), "1957-05-10"), new PickleCell(mock(PickleLocation.class), "Sid Vicious"))));
        return rows;
    }

    private List<PickleRow> listOfDoublesWithoutHeader() {
        List<PickleRow> rows = new ArrayList<PickleRow>();
        rows.add(new PickleRow(asList(new PickleCell(mock(PickleLocation.class), "100.5"), new PickleCell(mock(PickleLocation.class), "99.5"))));
        rows.add(new PickleRow(asList(new PickleCell(mock(PickleLocation.class), "0.5"), new PickleCell(mock(PickleLocation.class), "-0.5"))));
        rows.add(new PickleRow(asList(new PickleCell(mock(PickleLocation.class), "1000"), new PickleCell(mock(PickleLocation.class), "999"))));
        return rows;
    }

    private List<PickleRow> transposedListOfDatesWithHeader() {
        List<PickleRow> rows = new ArrayList<PickleRow>();
        rows.add(new PickleRow(asList(new PickleCell(mock(PickleLocation.class), "Birth Date"), new PickleCell(mock(PickleLocation.class), "1957-05-10"))));
        return rows;
    }

    private List<PickleRow> transposedListOfDatesAndCalWithHeader() {
        List<PickleRow> rows = new ArrayList<PickleRow>();
        rows.add(new PickleRow(asList(new PickleCell(mock(PickleLocation.class), "Birth Date"), new PickleCell(mock(PickleLocation.class), "1957-05-10"), new PickleCell(mock(PickleLocation.class), ""))));
        rows.add(new PickleRow(asList(new PickleCell(mock(PickleLocation.class), "Death Cal"), new PickleCell(mock(PickleLocation.class), "1979-02-02"), new PickleCell(mock(PickleLocation.class), ""))));
        return rows;
    }

    private List<PickleRow> transposedListOfDatesAndNamesWithHeader() {
        List<PickleRow> rows = new ArrayList<PickleRow>();
        rows.add(new PickleRow(asList(new PickleCell(mock(PickleLocation.class), "Birth Date"), new PickleCell(mock(PickleLocation.class), "1957-05-10"))));
        rows.add(new PickleRow(asList(new PickleCell(mock(PickleLocation.class), "Name"), new PickleCell(mock(PickleLocation.class), "Sid Vicious"))));
        return rows;
    }

    private List<PickleRow> transposedListOfDoublesWithoutHeader() {
        List<PickleRow> rows = new ArrayList<PickleRow>();
        rows.add(new PickleRow(asList(new PickleCell(mock(PickleLocation.class), "100.5"), new PickleCell(mock(PickleLocation.class), "0.5"), new PickleCell(mock(PickleLocation.class), "1000"))));
        rows.add(new PickleRow(asList(new PickleCell(mock(PickleLocation.class), "99.5"), new PickleCell(mock(PickleLocation.class), "-0.5"), new PickleCell(mock(PickleLocation.class), "999"))));
        return rows;
    }

    private Date sidsBirthday() {
        Calendar sidsBirthday = Calendar.getInstance();
        sidsBirthday.set(1957, 4, 10, 0, 0, 0);
        sidsBirthday.set(Calendar.MILLISECOND, 0);
        return sidsBirthday.getTime();
    }

    private Calendar sidsDeathcal() {
        Calendar sidsDeathcal = Calendar.getInstance();
        sidsDeathcal.set(1979, 1, 2, 0, 0, 0);
        sidsDeathcal.set(Calendar.MILLISECOND, 0);
        return sidsDeathcal;
    }

    public static class UserPojo {
        private Date birthDate;
        private Calendar deathCal;
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

    public static class UserWithNameField {
        public Name name;
        public Date birthDate;
    }

    public static class PrimitiveContainer {
        public Integer number;
        public Boolean bool;
        public boolean bool2;
    }

    @XStreamConverter(NameConverter.class)
    public static class Name {
        public String first;
        public String last;
    }

    public static class NameConverter extends Transformer<Name> {
        @Override
        public Name transform(String value) {
            Name name = new Name();
            String[] firstLast = value.split(" ");
            name.first = firstLast[0];
            name.last = firstLast[1];
            return name;
        }
    }
}
