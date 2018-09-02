package io.cucumber.needle.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import javax.inject.Inject;

import cucumber.api.java.Before;
import io.cucumber.needle.test.injectionprovider.NameGetter;
import io.cucumber.needle.test.injectionprovider.SimpleNameGetterProvider;

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
