package io.cucumber.compatibility.datatables;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DataTables {

    private DataTable toTranspose;

    @When("the following table is transposed:")
    public void theFollowingTableIsTransposed(DataTable toTranspose) {
        this.toTranspose = toTranspose;
    }

    @Then("it should be:")
    public void itShouldBe(DataTable expected) {
        assertEquals(expected, toTranspose.transpose());
    }

}
