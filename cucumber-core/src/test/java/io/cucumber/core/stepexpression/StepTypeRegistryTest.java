package io.cucumber.core.stepexpression;

import com.fasterxml.jackson.databind.JsonNode;
import io.cucumber.cucumberexpressions.Expression;
import io.cucumber.cucumberexpressions.ExpressionFactory;
import io.cucumber.cucumberexpressions.ParameterByTypeTransformer;
import io.cucumber.cucumberexpressions.ParameterType;
import io.cucumber.datatable.DataTable;
import io.cucumber.datatable.DataTableType;
import io.cucumber.datatable.TableCellByTypeTransformer;
import io.cucumber.datatable.TableEntryByTypeTransformer;
import io.cucumber.docstring.DocStringType;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static java.util.Locale.ENGLISH;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

class StepTypeRegistryTest {

    private final StepTypeRegistry registry = new StepTypeRegistry(ENGLISH);
    private final ExpressionFactory expressionFactory = new ExpressionFactory(registry.parameterTypeRegistry());

    @Test
    void should_define_parameter_type() {
        ParameterType<Object> expected = new ParameterType<>(
            "example",
            ".*",
            Object.class,
            (String s) -> null);
        registry.defineParameterType(expected);
        Expression expresion = expressionFactory.createExpression("{example}");
        assertThat(expresion.getRegexp().pattern(), is("^(.*)$"));
    }

    @Test
    void should_define_data_table_parameter_type() {
        DataTableType expected = new DataTableType(Date.class, (DataTable dataTable) -> null);
        registry.defineDataTableType(expected);
    }

    @Test
    void should_define_doc_string_parameter_type() {
        DocStringType expected = new DocStringType(JsonNode.class, "json", (String s) -> null);
        registry.defineDocStringType(expected);
    }

    @Test
    void should_set_default_parameter_transformer() {
        ParameterByTypeTransformer expected = (fromValue, toValueType) -> null;
        registry.setDefaultParameterTransformer(expected);
    }

    @Test
    void should_set_default_table_cell_transformer() {
        TableCellByTypeTransformer expected = (cell, toValueType) -> null;
        registry.setDefaultDataTableCellTransformer(expected);
    }

    @Test
    void should_set_default_table_entry_transformer() {
        TableEntryByTypeTransformer expected = (entry, toValueType, tableCellByTypeTransformer) -> null;
        registry.setDefaultDataTableEntryTransformer(expected);
    }

}
