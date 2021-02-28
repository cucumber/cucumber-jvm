package io.cucumber.needle.test;

import io.cucumber.java.Before;
import io.cucumber.needle.test.injectionprovider.NameGetter;
import io.cucumber.needle.test.injectionprovider.SimpleNameGetterProvider;

import javax.inject.Inject;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

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
