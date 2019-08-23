package io.cucumber.core.stepexpression;

import io.cucumber.core.exception.CucumberException;
import io.cucumber.cucumberexpressions.Expression;
import io.cucumber.cucumberexpressions.ExpressionFactory;
import io.cucumber.cucumberexpressions.ParameterByTypeTransformer;
import io.cucumber.cucumberexpressions.ParameterType;
import io.cucumber.datatable.DataTable;
import io.cucumber.datatable.DataTableType;
import io.cucumber.datatable.TableCellByTypeTransformer;
import io.cucumber.datatable.TableEntryByTypeTransformer;
import org.junit.jupiter.api.Test;

import java.util.Date;
import org.junit.jupiter.api.function.Executable;

import static java.util.Locale.ENGLISH;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TypeRegistryTest {

    private final TypeRegistry registry = new TypeRegistry(ENGLISH);
    private final ExpressionFactory expressionFactory = new ExpressionFactory(registry.parameterTypeRegistry());

    @Test
    void should_define_parameter_type() {
        ParameterType<Object> expected = new ParameterType<>(
            "example",
            ".*",
            Object.class,
            (String s) -> null
        );
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
        String contentType = "sb";
        DocStringType expected = new DocStringType(StringBuilder.class, contentType, (String s) -> new StringBuilder(s));
        registry.defineDocStringType(expected);
    }

    @Test
    void should_not_define_empty_doc_string_type() {
        String docString = "A rather long and boring string of documentation";
        String contentType = "";
        Executable testMethod = () ->registry.defineDocStringType(new DocStringType(StringBuilder.class, contentType, (String s) -> new StringBuilder(s)));
        CucumberException actualThrown = assertThrows(CucumberException.class, testMethod);
        assertThat("Unexpected exception message", actualThrown.getMessage(), is(equalTo(
            "There is already docstring type registered for content type \"\".\n" +
                "It registered as class java.lang.String. You are trying to add a class java.lang.StringBuilder"
        )));
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
