package billing;

import com.google.inject.Inject;
import cuke4duke.annotation.I18n.EN.Given;
import cuke4duke.annotation.I18n.EN.Then;
import cuke4duke.annotation.I18n.EN.When;

import java.math.BigDecimal;

import static org.junit.Assert.assertTrue;

/**
 * @author Henning Jensen
 */
public class BillingSteps {

    @Inject
    private BillingService billingService;

    private Transaction transaction;

    private CreateTransactionResponse response;

    @Given("^I have a transaction$")
    public void iHaveATransaction() {
        if (transaction != null) {
            throw new RuntimeException("Wait what? The instance is shared across scenarios?");
        }
        transaction = new Transaction("12345678", new BigDecimal("50.00"));
    }

    @When("^I send the transaction to billing$")
    public void iSendTheTransactionToBilling() {
        response = billingService.sendTransactionToBilling(transaction);
    }

    @Then("^the response should be OK$")
    public void theResponseShouldBeOK() {
        assertTrue(response.isOK());
    }

}
