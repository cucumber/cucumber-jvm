package io.cucumber.needle.test;

import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import de.akquinet.jbosscc.needle.injection.InjectionProvider;
import io.cucumber.needle.DefaultInstanceInjectionProvider;
import io.cucumber.needle.NeedleInjectionProvider;

import javax.inject.Inject;

import static org.junit.Assert.assertTrue;

/**
 * Just here to show that injection providers from this class also work in other classes.
 *
 * @author Lars Bilger
 */
public class MoreSteps {

    @NeedleInjectionProvider
    private InjectionProvider<MoreSteps> thisInjectionProvider = new DefaultInstanceInjectionProvider<MoreSteps>(this);

    @Inject
    private AtmWithdrawalSteps atmWithdrawalSteps;

    @Before
    public void checkInjectionWorked() {
        assertTrue("Got a mock injected instead of the real instance.", atmWithdrawalSteps.isThisReallyYouOrJustAMock());
    }

    @Given("i call a step that i don't really need")
    public void this_step_is_here_only_to_have_the_class_instantiated() {
    }

    public boolean isThisReallyYouOrJustAMock() {
        return true;
    }
}
