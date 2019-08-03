package io.cucumber.core.runner;

import gherkin.pickles.PickleCell;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleRow;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleString;
import gherkin.pickles.PickleTable;
import io.cucumber.core.stepexpression.Argument;
import io.cucumber.core.stepexpression.TypeRegistry;
import io.cucumber.datatable.DataTable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class CoreStepDefinitionTest {

    private final TypeRegistry typeRegistry = new TypeRegistry(Locale.ENGLISH);

    @Test
    public void should_apply_identity_transform_to_doc_string_when_target_type_is_object() {
        StubStepDefinition stub = new StubStepDefinition("I have some step", Object.class);
        CoreStepDefinition stepDefinition = new CoreStepDefinition(stub, typeRegistry);

        PickleString pickleString = new PickleString(null, "content", "text");
        List<Argument> arguments = stepDefinition.matchedArguments(new PickleStep("I have some step", singletonList(pickleString), emptyList()));
        assertEquals("content", arguments.get(0).getValue());
    }

    @Test
    public void should_apply_identity_transform_to_data_table_when_target_type_is_object() {
        StubStepDefinition stub = new StubStepDefinition("I have some step", Object.class);
        CoreStepDefinition stepDefinition = new CoreStepDefinition(stub, typeRegistry);

        PickleTable table = new PickleTable(singletonList(new PickleRow(singletonList(new PickleCell(null, "content")))));
        List<Argument> arguments = stepDefinition.matchedArguments(new PickleStep("I have some step", singletonList(table), emptyList()));
        assertEquals(DataTable.create(singletonList(singletonList("content"))), arguments.get(0).getValue());
    }


    public static class StepDefs {
        public void listOfListOfDoubles(List<List<Double>> listOfListOfDoubles) {
        }

        public void plainDataTable(DataTable dataTable) {
        }

        public void mapOfDoubleToDouble(Map<Double, Double> mapOfDoubleToDouble) {
        }

        public void transposedMapOfDoubleToListOfDouble(Map<Double, List<Double>> mapOfDoubleToListOfDouble) {
        }

    }

    @Test
    public void transforms_to_map_of_double_to_double() throws Throwable {
        Method m = StepDefs.class.getMethod("mapOfDoubleToDouble", Map.class);
        Map<Double, Double> stepDefs = runStepDef(m, false, new PickleTable(listOfDoublesWithoutHeader()));

        assertThat(stepDefs, hasEntry(1000.0, 999.0));
        assertThat(stepDefs, hasEntry(0.5, -0.5));
        assertThat(stepDefs, hasEntry(100.5, 99.5));
    }

    @Test
    public void transforms_transposed_to_map_of_double_to_double() throws Throwable {
        Method m = StepDefs.class.getMethod("transposedMapOfDoubleToListOfDouble", Map.class);
        Map<Double, List<Double>> stepDefs = runStepDef(m, true, new PickleTable(listOfDoublesWithoutHeader()));
        assertThat(stepDefs, hasEntry(100.5, asList(0.5, 1000.0)));
    }

    @Test
    public void transforms_to_list_of_single_values() throws Throwable {
        Method m = StepDefs.class.getMethod("listOfListOfDoubles", List.class);
        List<List<Double>> stepDefs = runStepDef(m, false, new PickleTable(listOfDoublesWithoutHeader()));
        assertEquals("[[100.5, 99.5], [0.5, -0.5], [1000.0, 999.0]]", stepDefs.toString());
    }

    @Test
    public void transforms_to_list_of_single_values_transposed() throws Throwable {
        Method m = StepDefs.class.getMethod("listOfListOfDoubles", List.class);
        List<List<Double>> stepDefs = runStepDef(m, true, new PickleTable(transposedListOfDoublesWithoutHeader()));
        assertEquals("[[100.5, 99.5], [0.5, -0.5], [1000.0, 999.0]]", stepDefs.toString());
    }

    @Test
    public void passes_plain_data_table() throws Throwable {
        Method m = StepDefs.class.getMethod("plainDataTable", DataTable.class);
        DataTable stepDefs = runStepDef(m, false, new PickleTable(listOfDatesWithHeader()));
        assertEquals("Birth Date", stepDefs.cell(0, 0));
        assertEquals("1957-05-10", stepDefs.cell(1, 0));
    }

    @Test
    public void passes_transposed_data_table() throws Throwable {
        Method m = StepDefs.class.getMethod("plainDataTable", DataTable.class);
        DataTable stepDefs = runStepDef(m, true, new PickleTable(listOfDatesWithHeader()));
        assertEquals("Birth Date", stepDefs.cell(0, 0));
        assertEquals("1957-05-10", stepDefs.cell(0, 1));
    }

    private <T> T runStepDef(Method method, boolean transposed, PickleTable table) throws Throwable {
        StubStepDefinition stub = new StubStepDefinition("some text", transposed, method.getGenericParameterTypes());
        CoreStepDefinition coreStepDefinition = new CoreStepDefinition(stub, typeRegistry);

        PickleStep stepWithTable = new PickleStep("some text", asList((gherkin.pickles.Argument) table), asList(mock(PickleLocation.class)));
        List<Argument> arguments = coreStepDefinition.matchedArguments(stepWithTable);

        List<Object> result = new ArrayList<>();
        for (Argument argument : arguments) {
            result.add(argument.getValue());
        }
        coreStepDefinition.getStepDefinition().execute(result.toArray(new Object[0]));

        return (T) stub.getArgs().get(0);
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
