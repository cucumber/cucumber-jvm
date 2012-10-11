package cucumber.examples.spring.txn;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class BellyStepdefs {
    @Autowired
    private BreakfastRepository breakfastRepository;

    @Given("^we had this for breakfast:$")
    public void we_had_this_for_breakfast(List<Breakfast> breakfasts) throws Throwable {
        breakfastRepository.save(breakfasts);
    }

    @Then("^the total number of breakfasts should be (\\d+)$")
    public void the_total_number_of_breakfasts_should_be(int total) throws Throwable {
        assertEquals(total, breakfastRepository.findAll().size());
    }
}
