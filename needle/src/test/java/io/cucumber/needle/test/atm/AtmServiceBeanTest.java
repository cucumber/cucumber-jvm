package io.cucumber.needle.test.atm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

class AtmServiceBeanTest {

    private final AtmServiceBean bean = new AtmServiceBean();

    @BeforeEach
    void setUp() {
        assertThat(bean.getAmount(), is(0));
    }

    @Test
    void shouldDeposit() {
        bean.deposit(1000);
        assertThat(bean.getAmount(), is(1000));
    }

    @Test
    void shouldWithdraw() {
        bean.deposit(1000);
        bean.withdraw(300);
        assertThat(bean.getAmount(), is(700));
    }

}
