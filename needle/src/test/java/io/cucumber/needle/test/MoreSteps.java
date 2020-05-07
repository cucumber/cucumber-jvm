package io.cucumber.needle.test;

import de.akquinet.jbosscc.needle.injection.InjectionProvider;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.needle.DefaultInstanceInjectionProvider;
import io.cucumber.needle.NeedleInjectionProvider;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Just here to show that injection providers from this class also work in other
 * classes.
 *
 * @author Lars Bilger
 */
public class MoreSteps {

    @NeedleInjectionProvider
    private InjectionProvider<MoreSteps> thisInjectionProvider = new DefaultInstanceInjectionProvider<>(this);

    @Inject
    private AtmWithdrawalSteps atmWithdrawalSteps;

    @Before
    public void checkInjectionWorked() {
        assertTrue(atmWithdrawalSteps.isThisReallyYouOrJustAMock(),
            "Got a mock injected instead of the real instance.");
    }

    @Given("i call a step that i don't really need")
    public void this_step_is_here_only_to_have_the_class_instantiated() {
    }

    boolean isThisReallyYouOrJustAMock() {
        return true;
    }

}
