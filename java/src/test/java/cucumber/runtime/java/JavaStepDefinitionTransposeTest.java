package cucumber.runtime.java;

import cucumber.api.Transpose;
import cucumber.api.java.ObjectFactory;
import io.cucumber.messages.Messages.PickleStep;
import io.cucumber.messages.Messages.PickleTable;
import io.cucumber.messages.Messages.PickleTableRow;
import cucumber.runtime.PickleStepDefinitionMatch;
import cucumber.runtime.StepDefinition;
import cucumber.runtime.StepDefinitionMatch;
import io.cucumber.datatable.DataTable;
import io.cucumber.stepexpression.Argument;
import io.cucumber.stepexpression.TypeRegistry;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static cucumber.runtime.PickleHelper.cell;
import static cucumber.runtime.PickleHelper.row;
import static cucumber.runtime.PickleHelper.table;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class JavaStepDefinitionTransposeTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private static final String ENGLISH = "en";
    private final TypeRegistry typeRegistry = new TypeRegistry(Locale.ENGLISH);

    public static class StepDefs {
        public List<List<Double>> listOfListOfDoubles;
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
        StepDefs stepDefs = runStepDef(m, table(listOfDoublesWithoutHeader()));
        assertEquals(Double.valueOf(999.0), stepDefs.mapOfDoubleToDouble.get(1000.0));
        assertEquals(Double.valueOf(-0.5), stepDefs.mapOfDoubleToDouble.get(0.5));
        assertEquals(Double.valueOf(99.5), stepDefs.mapOfDoubleToDouble.get(100.5));
    }

    @Test
    public void transforms_transposed_to_map_of_double_to_double() throws Throwable {
        Method m = StepDefs.class.getMethod("transposedMapOfDoubleToListOfDouble", Map.class);
        StepDefs stepDefs = runStepDef(m, table(listOfDoublesWithoutHeader()));
        assertEquals(asList(0.5, 1000.0), stepDefs.mapOfDoubleToListOfDouble.get(100.5));
    }

    @Test
    public void transforms_to_list_of_single_values() throws Throwable {
        Method m = StepDefs.class.getMethod("listOfListOfDoubles", List.class);
        StepDefs stepDefs = runStepDef(m, table(listOfDoublesWithoutHeader()));
        assertEquals("[[100.5, 99.5], [0.5, -0.5], [1000.0, 999.0]]", stepDefs.listOfListOfDoubles.toString());
    }

    @Test
    public void transforms_to_list_of_single_values_transposed() throws Throwable {
        Method m = StepDefs.class.getMethod("listOfListOfDoublesTransposed", List.class);
        StepDefs stepDefs = runStepDef(m, table(transposedListOfDoublesWithoutHeader()));
        assertEquals("[[100.5, 99.5], [0.5, -0.5], [1000.0, 999.0]]", stepDefs.listOfListOfDoubles.toString());
    }

    @Test
    public void passes_plain_data_table() throws Throwable {
        Method m = StepDefs.class.getMethod("plainDataTable", DataTable.class);
        StepDefs stepDefs = runStepDef(m, table(listOfDatesWithHeader()));
        assertEquals("Birth Date", stepDefs.dataTable.cell(0, 0));
        assertEquals("1957-05-10", stepDefs.dataTable.cell(1, 0));
    }

    @Test
    public void passes_transposed_data_table() throws Throwable {
        Method m = StepDefs.class.getMethod("transposedDataTable", DataTable.class);
        StepDefs stepDefs = runStepDef(m, table(listOfDatesWithHeader()));
        assertEquals("Birth Date", stepDefs.dataTable.cell(0, 0));
        assertEquals("1957-05-10", stepDefs.dataTable.cell(0, 1));
    }

    private StepDefs runStepDef(Method method, PickleTable table) throws Throwable {
        StepDefs stepDefs = new StepDefs();
        ObjectFactory objectFactory = new SingletonFactory(stepDefs);

        StepDefinition stepDefinition = new JavaStepDefinition(method, "some text", 0, objectFactory, typeRegistry);
        PickleStep stepWithTable = PickleStep.newBuilder()
            .setText("some text")
            .setDataTable(table)
            .build();
        List<Argument> arguments = stepDefinition.matchedArguments(stepWithTable);

        StepDefinitionMatch stepDefinitionMatch = new PickleStepDefinitionMatch(arguments, stepDefinition, "some.feature", stepWithTable);
        stepDefinitionMatch.runStep(ENGLISH, null);
        return stepDefs;
    }

    private List<PickleTableRow> listOfDatesWithHeader() {
        return asList(
            row(cell("Birth Date")),
            row(cell("1957-05-10"))
        );
    }

    private List<PickleTableRow> listOfDoublesWithoutHeader() {
        return asList(
            row(cell("100.5"), cell("99.5")),
            row(cell("0.5"), cell("-0.5")),
            row(cell("1000"), cell("999"))
        );
    }

    private List<PickleTableRow> transposedListOfDoublesWithoutHeader() {
        return asList(
            row(cell("100.5"), cell("0.5"), cell("1000")),
            row(cell("99.5"), cell("-0.5"), cell("999"))
        );
    }

}
