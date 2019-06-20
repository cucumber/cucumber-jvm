package io.cucumber.java8;

import gherkin.pickles.PickleCell;
import gherkin.pickles.PickleRow;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleString;
import gherkin.pickles.PickleTable;
import io.cucumber.core.exception.CucumberException;
import io.cucumber.core.stepexpression.Argument;
import io.cucumber.core.stepexpression.TypeRegistry;
import io.cucumber.datatable.DataTable;
import org.junit.Test;

import java.util.List;
import java.util.Locale;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class Java8LambdaStepDefinitionTest {

    private final TypeRegistry typeRegistry = new TypeRegistry(Locale.ENGLISH);

    @Test
    public void should_calculate_parameters_count_from_body_with_one_param() {
        StepdefBody.A1<String> body = p1 -> {
        };
        Java8StepDefinition def = Java8StepDefinition.create("I have some step", StepdefBody.A1.class, body, typeRegistry);
        assertEquals(Integer.valueOf(1), def.getParameterCount());
    }

    @Test
    public void should_calculate_parameters_count_from_body_with_two_params() {
        StepdefBody.A2<String, String> body = (p1, p2) -> {
        };
        Java8StepDefinition def = Java8StepDefinition.create("I have some step", StepdefBody.A2.class, body, typeRegistry);
        assertEquals(Integer.valueOf(2), def.getParameterCount());
    }

    @Test
    public void should_apply_identity_transform_to_doc_string_when_target_type_is_object() {
        StepdefBody.A1 body = (p1) -> {
        };
        Java8StepDefinition def = Java8StepDefinition.create("I have some step", StepdefBody.A1.class, body, typeRegistry);
        PickleString pickleString = new PickleString(null, "content", "text");
        List<Argument> arguments = def.matchedArguments(new PickleStep("I have some step", singletonList(pickleString), emptyList()));
        assertEquals("content", arguments.get(0).getValue());
    }

    @Test
    public void should_apply_identity_transform_to_data_table_when_target_type_is_object() {
        StepdefBody.A1 body = (p1) -> {
        };
        Java8StepDefinition def = Java8StepDefinition.create("I have some step", StepdefBody.A1.class, body, typeRegistry);
        PickleTable table = new PickleTable(singletonList(new PickleRow(singletonList(new PickleCell(null, "content")))));
        List<Argument> arguments = def.matchedArguments(new PickleStep("I have some step", singletonList(table), emptyList()));
        assertEquals(DataTable.create(singletonList(singletonList("content"))), arguments.get(0).getValue());
    }


    @Test
    public void should_fail_for_param_with_non_generic_list() {
        try {
            StepdefBody.A1<List> body = p1 -> {
            };
            Java8StepDefinition.create("I have some step", StepdefBody.A1.class, body, typeRegistry);
        } catch (CucumberException expected) {
            assertEquals("Can't use java.util.List in lambda step definition. Declare a DataTable argument instead and convert manually with asList/asLists/asMap/asMaps", expected.getMessage());
        }
    }

    @Test
    public void should_fail_for_param_with_generic_list() {
        try {
            StepdefBody.A1<List<String>> body = p1 -> {
            };
            Java8StepDefinition.create("I have some step", StepdefBody.A1.class, body, typeRegistry);
        } catch (CucumberException expected) {
            assertEquals("Can't use java.util.List in lambda step definition. Declare a DataTable argument instead and convert manually with asList/asLists/asMap/asMaps", expected.getMessage());
        }
    }
}
