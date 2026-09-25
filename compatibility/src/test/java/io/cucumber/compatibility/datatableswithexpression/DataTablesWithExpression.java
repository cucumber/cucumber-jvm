package io.cucumber.compatibility.datatableswithexpression;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.When;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class DataTablesWithExpression {

    @When("a {string} with a table")
    public void theFollowingTableIsTransposed(String string, DataTable table) {
        assertEquals("Cucumber", string);
        assertEquals(DataTable.create(List.of(List.of("Species", "Cucumis sativus"))), table);
    }
}
