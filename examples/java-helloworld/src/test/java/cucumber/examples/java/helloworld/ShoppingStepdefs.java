package cucumber.examples.java.helloworld;

import cucumber.annotation.en.Given;
import cucumber.annotation.en.Then;
import cucumber.annotation.en.When;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ShoppingStepdefs {
    private final ShoppingList shoppingList = new ShoppingList();
    private StringBuilder printedList;

    @Given("^a shopping list:$")
    public void a_shopping_list(List<ShoppingItem> items) throws Throwable {
        for (ShoppingItem item : items) {
            shoppingList.addItem(item.name, item.count);
        }
    }

    @When("^I print that list$")
    public void I_print_that_list() throws Throwable {
        printedList = new StringBuilder();
        shoppingList.print(printedList);
    }

    @Then("^it should look like:$")
    public void it_should_look_like(String expected) throws Throwable {
        assertEquals(expected, printedList.toString());
    }

    // When converting tables to a List of objects it's usually better to
    // use classes that are only used in test (not in production). This
    // reduces coupling between scenarios and domain and gives you more control.
    public static class ShoppingItem {
        private String name;
        private Integer count;
    }
}
