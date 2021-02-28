package io.cucumber.needle.test;

import de.akquinet.jbosscc.needle.annotation.ObjectUnderTest;
import de.akquinet.jbosscc.needle.injection.InjectionProvider;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.needle.DefaultInstanceInjectionProvider;
import io.cucumber.needle.InjectionProviderInstancesSupplier;
import io.cucumber.needle.NeedleInjectionProvider;
import io.cucumber.needle.test.atm.AtmService;
import io.cucumber.needle.test.atm.AtmServiceBean;
import io.cucumber.needle.test.atm.BicGetter;
import io.cucumber.needle.test.injectionprovider.ValueInjectionProvider;
import org.hamcrest.core.Is;

import javax.inject.Inject;

import static java.util.Collections.singleton;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class AtmWithdrawalSteps {

    private static final String VALUE = "Value-per-constructor";

    private static final String BIC = "12345";
    /*
     * Provider instance will be added dynamically.
     */
    @NeedleInjectionProvider
    private final InjectionProvider<?> valueProvider = new ValueInjectionProvider(VALUE);
    @NeedleInjectionProvider
    private final InjectionProviderInstancesSupplier thisInjectionProviderSupplier = () -> singleton(
        new DefaultInstanceInjectionProvider<>(AtmWithdrawalSteps.this));
    /*
     * Inject will be mocked.
     */
    @Inject
    private BicGetter bicGetter;
    @Inject
    private MoreSteps moreSteps;
    /*
     * This is what we test
     */
    @ObjectUnderTest(implementation = AtmServiceBean.class)
    private AtmService atmService;

    @Given("I have {int} EUR in my account")
    public void I_have_EUR_in_my_account(final int account) {
        assertNotNull(atmService);
        when(bicGetter.getBic()).thenReturn(BIC);
        assertThat(atmService.getInfo(), is("BIC: " + BIC + " and VALUE: " + VALUE));

        assert (atmService.getAmount() == 0);
        atmService.deposit(account);
        assert (atmService.getAmount() == account);
    }

    @When("I withdraw {int} EUR")
    public void I_withdraw_EUR(final int amount) {
        atmService.withdraw(amount);
    }

    @Then("I have {int} EUR remaining.")
    public void I_have_EUR_remaining(final int remaining) {
        atmService.getAmount();
        assertThat(atmService.getAmount(), Is.is(remaining));
    }

    @Before
    public void checkInjectionWorked() {
        assertTrue(moreSteps.isThisReallyYouOrJustAMock(), "Got a mock injected instead of the real instance.");
    }

    public boolean isThisReallyYouOrJustAMock() {
        return true;
    }

}
