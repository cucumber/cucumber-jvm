package io.cucumber.compatibility.datatablesdocstrings;

import io.cucumber.datatable.DataTable;
import io.cucumber.docstring.DocString;
import io.cucumber.java.en.When;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class DataTableDocStrings {

    @When("a step with a data table a doc string")
    public void aStepWithADataTableADocString(DataTable table, DocString string) {
        assertEquals(DataTable.create(List.of(List.of("hello"))), table);
        assertEquals("world", string.getContent());
    }

    @When("a step with a doc string a data table")
    public void aStepWithADocStringADataTable(DocString string, DataTable table) {
        assertEquals("hello", string.getContent());
        assertEquals(DataTable.create(List.of(List.of("world"))), table);
    }
}
