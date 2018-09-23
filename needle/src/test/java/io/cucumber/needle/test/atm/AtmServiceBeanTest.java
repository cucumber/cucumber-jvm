package io.cucumber.needle.test.atm;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

public class AtmServiceBeanTest {

    private final AtmServiceBean bean = new AtmServiceBean();

    @Before
    public void setUp() {
        assertThat(bean.getAmount(), is(0));
    }

    @Test
    public void shouldDeposit() {
        bean.deposit(1000);
        assertThat(bean.getAmount(), is(1000));
    }

    @Test
    public void shouldWithdraw() {
        bean.deposit(1000);
        bean.withdraw(300);
        assertThat(bean.getAmount(), is(700));
    }

}
