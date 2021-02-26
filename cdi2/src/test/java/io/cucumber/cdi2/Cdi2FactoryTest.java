package io.cucumber.cdi2;

import io.cucumber.core.backend.ObjectFactory;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class Cdi2FactoryTest {

    final ObjectFactory factory = new Cdi2Factory();

    @Test
    void shouldCreateNewInstancesForEachScenario() {
        factory.addClass(VetoedBean.class);

        // Scenario 1
        factory.start();
        VetoedBean a1 = factory.getInstance(VetoedBean.class);
        VetoedBean a2 = factory.getInstance(VetoedBean.class);
        assertThat(a1, is(equalTo(a2)));
        factory.stop();

        // Scenario 2
        factory.start();
        VetoedBean b1 = factory.getInstance(VetoedBean.class);
        factory.stop();

        // VetoedBean makes it possible to compare the object outside the
        // scenario/application scope
        assertAll(
                () -> assertThat(a1, is(notNullValue())),
                () -> assertThat(a1, is(not(equalTo(b1)))),
                () -> assertThat(b1, is(not(equalTo(a1)))));
    }

    @Test
    void shouldCreateApplicationScopedInstance() {
        factory.addClass(ApplicationScopedBean.class);
        factory.start();
        ApplicationScopedBean cdiStep = factory.getInstance(ApplicationScopedBean.class);
        assertAll(
                // assert that it is is a CDI proxy
                () -> assertThat(cdiStep.getClass(), not(is(ApplicationScopedBean.class))),
                () -> assertThat(cdiStep.getClass().getSuperclass(), is(ApplicationScopedBean.class)));
        factory.stop();
    }

    @Test
    void shouldCreateUnmanagedInstance() {
        factory.addClass(UnmanagedBean.class);
        factory.start();
        assertNotNull(factory.getInstance(UnmanagedBean.class));
        factory.stop();
    }

}
