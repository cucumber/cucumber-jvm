package io.cucumber.needle.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.cucumber.needle.InjectionProviderInstancesSupplier;
import io.cucumber.needle.NeedleInjectionProvider;
import io.cucumber.needle.DefaultInstanceInjectionProvider;
import io.cucumber.needle.test.atm.AtmService;
import io.cucumber.needle.test.atm.AtmServiceBean;
import io.cucumber.needle.test.atm.BicGetter;
import io.cucumber.needle.test.injectionprovider.ValueInjectionProvider;
import de.akquinet.jbosscc.needle.annotation.ObjectUnderTest;
import de.akquinet.jbosscc.needle.injection.InjectionProvider;
import org.hamcrest.core.Is;

import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;

public class AtmWithdrawalSteps {

    private static final String VALUE = "Value-per-constructor";

    private static final String BIC = "12345";
    /*
     * Inject will be mocked.
     */
    @Inject
    private BicGetter bicGetter;

    @Inject
    private MoreSteps moreSteps;

    /*
     * Provider instance will be added dynamically.
     */
    @NeedleInjectionProvider
    private final InjectionProvider<?> valueProvider = new ValueInjectionProvider(VALUE);

    @NeedleInjectionProvider
    private final InjectionProviderInstancesSupplier thisInjectionProviderSupplier = new InjectionProviderInstancesSupplier() {
        @Override
        public Set<InjectionProvider<?>> get() {
            return Collections.<InjectionProvider<?>>singleton(new DefaultInstanceInjectionProvider<AtmWithdrawalSteps>(AtmWithdrawalSteps.this));
        }
    };

    /*
     * This is what we test
     */
    @ObjectUnderTest(implementation = AtmServiceBean.class)
    private AtmService atmService;

    @Given("I have {int} EUR in my account")
    public void I_have_EUR_in_my_account(final int account) throws Throwable {
        assertNotNull(atmService);
        when(bicGetter.getBic()).thenReturn(BIC);
        assertThat(atmService.getInfo(), is("BIC: " + BIC + " and VALUE: " + VALUE));

        assert (atmService.getAmount() == 0);
        atmService.deposit(account);
        assert (atmService.getAmount() == account);
    }

    @When("I withdraw {int} EUR")
    public void I_withdraw_EUR(final int amount) throws Throwable {
        atmService.withdraw(amount);
    }

    @Then("I have {int} EUR remaining.")
    public void I_have_EUR_remaining(final int remaining) throws Throwable {
        atmService.getAmount();
        assertThat(atmService.getAmount(), Is.is(remaining));
    }

    @Before
    public void checkInjectionWorked() {
        assertTrue("Got a mock injected instead of the real instance.", moreSteps.isThisReallyYouOrJustAMock());
    }

    public boolean isThisReallyYouOrJustAMock() {
        return true;
    }
}
