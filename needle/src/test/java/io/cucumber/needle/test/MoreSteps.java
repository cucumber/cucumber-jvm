package io.cucumber.needle.test;

import static org.junit.Assert.assertTrue;

import io.cucumber.java.api.Before;
import io.cucumber.java.api.en.Given;
import io.cucumber.needle.api.NeedleInjectionProvider;
import io.cucumber.needle.DefaultInstanceInjectionProvider;
import de.akquinet.jbosscc.needle.injection.InjectionProvider;

import javax.inject.Inject;

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
