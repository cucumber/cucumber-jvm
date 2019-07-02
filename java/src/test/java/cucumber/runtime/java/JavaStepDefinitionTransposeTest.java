package cucumber.runtime.java;

import cucumber.api.Transpose;
import cucumber.api.java.ObjectFactory;
import cucumber.runtime.StepDefinition;
import gherkin.pickles.PickleCell;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleRow;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleTable;
import io.cucumber.datatable.DataTable;
import io.cucumber.stepexpression.Argument;
import io.cucumber.stepexpression.TypeRegistry;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class JavaStepDefinitionTransposeTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final TypeRegistry typeRegistry = new TypeRegistry(Locale.ENGLISH);

    public static class StepDefs {
        List<List<Double>> listOfListOfDoubles;
        public Map<Double, Double> mapOfDoubleToDouble;

        public DataTable dataTable;
        private Map<Double, List<Double>> mapOfDoubleToListOfDouble;


        public void listOfListOfDoubles(List<List<Double>> listOfListOfDoubles) {
            this.listOfListOfDoubles = listOfListOfDoubles;
        }

        public void listOfListOfDoublesTransposed(@Transpose List<List<Double>> listOfListOfDoubles) {
            this.listOfListOfDoubles = listOfListOfDoubles;
        }

        public void plainDataTable(DataTable dataTable) {
            this.dataTable = dataTable;
        }

        public void transposedDataTable(@Transpose DataTable dataTable) {
            this.dataTable = dataTable;
        }

        public void newtransposedAnnotationDataTable(@io.cucumber.java.Transpose DataTable dataTable) {
            this.dataTable = dataTable;
        }

        public void mapOfDoubleToDouble(Map<Double, Double> mapOfDoubleToDouble) {
            this.mapOfDoubleToDouble = mapOfDoubleToDouble;
        }

        public void transposedMapOfDoubleToListOfDouble(@Transpose Map<Double, List<Double>> mapOfDoubleToListOfDouble) {
            this.mapOfDoubleToListOfDouble = mapOfDoubleToListOfDouble;
        }
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
    public void transforms_transposed_to_map_of_double_to_double() throws Throwable {
        Method m = StepDefs.class.getMethod("transposedMapOfDoubleToListOfDouble", Map.class);
        StepDefs stepDefs = runStepDef(m, new PickleTable(listOfDoublesWithoutHeader()));
        assertEquals(asList(0.5, 1000.0), stepDefs.mapOfDoubleToListOfDouble.get(100.5));
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
    public void passes_plain_data_table() throws Throwable {
        Method m = StepDefs.class.getMethod("plainDataTable", DataTable.class);
        StepDefs stepDefs = runStepDef(m, new PickleTable(listOfDatesWithHeader()));
        assertEquals("Birth Date", stepDefs.dataTable.cell(0, 0));
        assertEquals("1957-05-10", stepDefs.dataTable.cell(1, 0));
    }

    @Test
    public void passes_transposed_data_table() throws Throwable {
        Method m = StepDefs.class.getMethod("transposedDataTable", DataTable.class);
        StepDefs stepDefs = runStepDef(m, new PickleTable(listOfDatesWithHeader()));
        assertEquals("Birth Date", stepDefs.dataTable.cell(0, 0));
        assertEquals("1957-05-10", stepDefs.dataTable.cell(0, 1));
    }

    @Test
    public void passes_newtransposedAnnotation_data_table() throws Throwable {
        Method m = StepDefs.class.getMethod("newtransposedAnnotationDataTable", DataTable.class);
        StepDefs stepDefs = runStepDef(m, new PickleTable(listOfDatesWithHeader()));
        assertEquals("Birth Date", stepDefs.dataTable.cell(0, 0));
        assertEquals("1957-05-10", stepDefs.dataTable.cell(0, 1));
    }

    private StepDefs runStepDef(Method method, PickleTable table) throws Throwable {
        StepDefs stepDefs = new StepDefs();
        ObjectFactory objectFactory = new SingletonFactory(stepDefs);

        StepDefinition stepDefinition = new JavaStepDefinition(method, "some text", 0, objectFactory, typeRegistry);
        PickleStep stepWithTable = new PickleStep("some text", asList((gherkin.pickles.Argument) table), asList(mock(PickleLocation.class)));
        List<Argument> arguments = stepDefinition.matchedArguments(stepWithTable);

        List<Object> result = new ArrayList<>();
        for (Argument argument : arguments) {
            result.add(argument.getValue());
        }
        stepDefinition.execute(result.toArray(new Object[0]));

        return stepDefs;
    }

    private List<PickleRow> listOfDatesWithHeader() {
        List<PickleRow> rows = new ArrayList<>();
        rows.add(new PickleRow(asList(new PickleCell(mock(PickleLocation.class), "Birth Date"))));
        rows.add(new PickleRow(asList(new PickleCell(mock(PickleLocation.class), "1957-05-10"))));
        return rows;
    }

    private List<PickleRow> listOfDoublesWithoutHeader() {
        List<PickleRow> rows = new ArrayList<>();
        rows.add(new PickleRow(asList(new PickleCell(mock(PickleLocation.class), "100.5"), new PickleCell(mock(PickleLocation.class), "99.5"))));
        rows.add(new PickleRow(asList(new PickleCell(mock(PickleLocation.class), "0.5"), new PickleCell(mock(PickleLocation.class), "-0.5"))));
        rows.add(new PickleRow(asList(new PickleCell(mock(PickleLocation.class), "1000"), new PickleCell(mock(PickleLocation.class), "999"))));
        return rows;
    }

    private List<PickleRow> transposedListOfDoublesWithoutHeader() {
        List<PickleRow> rows = new ArrayList<>();
        rows.add(new PickleRow(asList(new PickleCell(mock(PickleLocation.class), "100.5"), new PickleCell(mock(PickleLocation.class), "0.5"), new PickleCell(mock(PickleLocation.class), "1000"))));
        rows.add(new PickleRow(asList(new PickleCell(mock(PickleLocation.class), "99.5"), new PickleCell(mock(PickleLocation.class), "-0.5"), new PickleCell(mock(PickleLocation.class), "999"))));
        return rows;
    }

}
