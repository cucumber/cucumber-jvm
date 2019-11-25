package io.cucumber.core.runner;

import io.cucumber.core.feature.CucumberFeature;
import io.cucumber.core.feature.CucumberStep;
import io.cucumber.core.feature.TestFeatureParser;
import io.cucumber.core.stepexpression.Argument;
import io.cucumber.core.stepexpression.StepTypeRegistry;
import io.cucumber.datatable.DataTable;
import io.cucumber.docstring.DocString;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CoreStepDefinitionTest {

    private final StepTypeRegistry stepTypeRegistry = new StepTypeRegistry(Locale.ENGLISH);

    @Test
    void should_apply_identity_transform_to_doc_string_when_target_type_is_object() {
        CucumberFeature feature = TestFeatureParser.parse("" +
            "Feature: Test feature\n" +
            "  Scenario: Test scenario\n" +
            "     Given I have some step\n" +
            "       \"\"\"\n" +
            "       content\n" +
            "       \"\"\"\n"
        );
        StubStepDefinition stub = new StubStepDefinition("I have some step", Object.class);
        CoreStepDefinition stepDefinition = new CoreStepDefinition(stub, stepTypeRegistry);
        CucumberStep step = feature.getPickles().get(0).getSteps().get(0);
        List<Argument> arguments = stepDefinition.matchedArguments(step);
        assertThat(arguments.get(0).getValue(), is(equalTo(DocString.create("content"))));
    }


    @Test
    void should_apply_identity_transform_to_data_table_when_target_type_is_object() {
        CucumberFeature feature = TestFeatureParser.parse("" +
            "Feature: Test feature\n" +
            "  Scenario: Test scenario\n" +
            "     Given I have some step\n" +
            "      | content |\n"
        );
        StubStepDefinition stub = new StubStepDefinition("I have some step", Object.class);
        CoreStepDefinition stepDefinition = new CoreStepDefinition(stub, stepTypeRegistry);
        List<Argument> arguments = stepDefinition.matchedArguments(feature.getPickles().get(0).getSteps().get(0));
        assertThat(arguments.get(0).getValue(), is(equalTo(DataTable.create(singletonList(singletonList("content"))))));
    }

    @Test
    void should_convert_empty_pickle_table_cells_to_null_values() {
        CucumberFeature feature = TestFeatureParser.parse("" +
            "Feature: Test feature\n" +
            "  Scenario: Test scenario\n" +
            "     Given I have some step\n" +
            "       |  |\n"
        );
        StubStepDefinition stub = new StubStepDefinition("I have some step", Object.class);
        CoreStepDefinition stepDefinition = new CoreStepDefinition(stub, stepTypeRegistry);
        List<Argument> arguments = stepDefinition.matchedArguments(feature.getPickles().get(0).getSteps().get(0));
        assertEquals(DataTable.create(singletonList(singletonList(null))), arguments.get(0).getValue());
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    public static class Steps {
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
    void transforms_to_map_of_double_to_double() throws Throwable {
        Method m = Steps.class.getMethod("mapOfDoubleToDouble", Map.class);
        CucumberFeature feature = TestFeatureParser.parse("" +
            "Feature: Test feature\n" +
            "  Scenario: Test scenario\n" +
            "     Given some text\n" +
            "      | 100.5 | 99.5 | \n" +
            "      | 0.5   | -0.5 | \n" +
            "      | 1000  | 999  | \n"
        );
        Map<Double, Double> stepDefs = runStepDef(m, false, feature);

        assertAll("Checking StepDefs",
            () -> assertThat(stepDefs, hasEntry(1000.0, 999.0)),
            () -> assertThat(stepDefs, hasEntry(0.5, -0.5)),
            () -> assertThat(stepDefs, hasEntry(100.5, 99.5))
        );
    }

    @Test
    void transforms_transposed_to_map_of_double_to_double() throws Throwable {
        Method m = Steps.class.getMethod("transposedMapOfDoubleToListOfDouble", Map.class);
        CucumberFeature feature = TestFeatureParser.parse("" +
            "Feature: Test feature\n" +
            "  Scenario: Test scenario\n" +
            "     Given some text\n" +
            "      | 100.5 | 99.5 | \n" +
            "      | 0.5   | -0.5 | \n" +
            "      | 1000  | 999  | \n"
        );
        Map<Double, List<Double>> stepDefs = runStepDef(m, true, feature);
        assertThat(stepDefs, hasEntry(100.5, asList(0.5, 1000.0)));
    }

    @Test
    void transforms_to_list_of_single_values() throws Throwable {
        Method m = Steps.class.getMethod("listOfListOfDoubles", List.class);
        CucumberFeature feature = TestFeatureParser.parse("" +
            "Feature: Test feature\n" +
            "  Scenario: Test scenario\n" +
            "     Given some text\n" +
            "      | 100.5 | 99.5 | \n" +
            "      | 0.5   | -0.5 | \n" +
            "      | 1000  | 999  | \n"
        );
        List<List<Double>> stepDefs = runStepDef(m, false, feature);
        assertThat(stepDefs.toString(), is(equalTo("[[100.5, 99.5], [0.5, -0.5], [1000.0, 999.0]]")));
    }

    @Test
    void transforms_to_list_of_single_values_transposed() throws Throwable {
        Method m = Steps.class.getMethod("listOfListOfDoubles", List.class);
        CucumberFeature feature = TestFeatureParser.parse("" +
            "Feature: Test feature\n" +
            "  Scenario: Test scenario\n" +
            "     Given some text\n" +
            "      | 100.5 | 0.5   | 1000| \n" +
            "      | 99.5   | -0.5 | 999 | \n"
        );
        List<List<Double>> stepDefs = runStepDef(m, true, feature);
        assertThat(stepDefs.toString(), is(equalTo("[[100.5, 99.5], [0.5, -0.5], [1000.0, 999.0]]")));
    }

    @Test
    void passes_plain_data_table() throws Throwable {
        Method m = Steps.class.getMethod("plainDataTable", DataTable.class);
        CucumberFeature feature = TestFeatureParser.parse("" +
            "Feature: Test feature\n" +
            "  Scenario: Test scenario\n" +
            "     Given some text\n" +
            "      | Birth Date | \n" +
            "      | 1957-05-10 | \n"
        );
        DataTable stepDefs = runStepDef(m, false, feature);

        assertAll("Checking stepDefs",
            () -> assertThat(stepDefs.cell(0, 0), is(equalTo("Birth Date"))),
            () -> assertThat(stepDefs.cell(1, 0), is(equalTo("1957-05-10")))
        );
    }

    @Test
    void passes_transposed_data_table() throws Throwable {
        Method m = Steps.class.getMethod("plainDataTable", DataTable.class);
        CucumberFeature feature = TestFeatureParser.parse("" +
            "Feature: Test feature\n" +
            "  Scenario: Test scenario\n" +
            "     Given some text\n" +
            "      | Birth Date | \n" +
            "      | 1957-05-10 | \n"
        );
        DataTable stepDefs = runStepDef(m, true, feature);

        assertAll("Checking stepDefs",
            () -> assertThat(stepDefs.cell(0, 0), is(equalTo("Birth Date"))),
            () -> assertThat(stepDefs.cell(0, 1), is(equalTo("1957-05-10")))
        );
    }

    @SuppressWarnings("unchecked")
    private <T> T runStepDef(Method method, boolean transposed, CucumberFeature feature) throws Throwable {
        StubStepDefinition stub = new StubStepDefinition("some text", transposed, method.getGenericParameterTypes());
        CoreStepDefinition coreStepDefinition = new CoreStepDefinition(stub, stepTypeRegistry);
        CucumberStep stepWithTable = feature.getPickles().get(0).getSteps().get(0);
        List<Argument> arguments = coreStepDefinition.matchedArguments(stepWithTable);

        List<Object> result = new ArrayList<>();
        for (Argument argument : arguments) {
            result.add(argument.getValue());
        }
        coreStepDefinition.getStepDefinition().execute(result.toArray(new Object[0]));

        return (T) stub.getArgs().get(0);
    }
}
