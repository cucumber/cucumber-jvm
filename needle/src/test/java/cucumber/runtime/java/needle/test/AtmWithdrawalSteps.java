package cucumber.runtime.java.needle.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import javax.inject.Inject;

import org.hamcrest.core.Is;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.api.needle.NeedleInjectionProvider;
import cucumber.runtime.java.needle.test.atm.AtmService;
import cucumber.runtime.java.needle.test.atm.AtmServiceBean;
import cucumber.runtime.java.needle.test.atm.BicGetter;
import cucumber.runtime.java.needle.test.injectionprovider.ValueInjectionProvider;
import de.akquinet.jbosscc.needle.annotation.ObjectUnderTest;
import de.akquinet.jbosscc.needle.injection.InjectionProvider;

public class AtmWithdrawalSteps {

    private static final String VALUE = "Value-per-constructor";

    private static final String BIC = "12345";
    /*
     * Inject will be mocked.
     */
    @Inject
    private BicGetter bicGetter;

    /*
     * Provider instance will be added dynamically.
     */
    @NeedleInjectionProvider
    private final InjectionProvider<?> valueProvider = new ValueInjectionProvider(VALUE);

    /*
     * This is what we test
     */
    @ObjectUnderTest(implementation = AtmServiceBean.class)
    private AtmService atmService;

    @Given("^I have (\\d+) EUR in my account$")
    public void I_have_EUR_in_my_account(final int account) throws Throwable {
        assertNotNull(atmService);
        when(bicGetter.getBic()).thenReturn(BIC);
        assertThat(atmService.getInfo(), is("BIC: " + BIC + " and VALUE: " + VALUE));

        assert (atmService.getAmount() == 0);
        atmService.deposit(account);
        assert (atmService.getAmount() == account);
    }

    @When("^I withdraw (\\d+) EUR$")
    public void I_withdraw_EUR(final int amount) throws Throwable {
        atmService.withdraw(amount);
    }

    @Then("^I have (\\d+) EUR remaining.$")
    public void I_have_EUR_remaining(final int remaining) throws Throwable {
        atmService.getAmount();
        assertThat(atmService.getAmount(), Is.is(remaining));
    }

}
