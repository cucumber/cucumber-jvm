package cucumber.runtime.java.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import javax.inject.Inject;

import cucumber.api.java.Before;
import cucumber.runtime.java.test.injectionprovider.NameGetter;
import cucumber.runtime.java.test.injectionprovider.SimpleNameGetterProvider;

public class AtmWithdrawalGlue {

    /*
     * Inject will be mocked.
     */
    @Inject
    private NameGetter fooGetter;

    @Before
    public void assertInjection() {
        assertThat(fooGetter.getName(), is(SimpleNameGetterProvider.FOO));
    }
}
