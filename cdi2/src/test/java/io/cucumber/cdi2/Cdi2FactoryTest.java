package io.cucumber.cdi2;

import io.cucumber.core.backend.ObjectFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Vetoed;
import javax.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class Cdi2FactoryTest {

    final ObjectFactory factory = new Cdi2Factory();

    @AfterEach
    void stop() {
        factory.stop();
    }

    @Test
    void lifecycleIsIdempotent() {
        assertDoesNotThrow(factory::stop);
        factory.start();
        assertDoesNotThrow(factory::start);
        factory.stop();
        assertDoesNotThrow(factory::stop);
    }

    @Vetoed
    static class VetoedBean {

    }

    @Test
    void shouldCreateNewInstancesForEachScenario() {
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

    @ApplicationScoped
    static class ApplicationScopedBean {

    }

    @Test
    void shouldCreateApplicationScopedInstance() {
        factory.start();
        ApplicationScopedBean bean = factory.getInstance(ApplicationScopedBean.class);
        assertAll(
            // assert that it is is a CDI proxy
            () -> assertThat(bean.getClass(), not(is(ApplicationScopedBean.class))),
            () -> assertThat(bean.getClass().getSuperclass(), is(ApplicationScopedBean.class)));
        factory.stop();
    }

    static class UnmanagedBean {

    }

    @Test
    void shouldCreateUnmanagedInstance() {
        factory.start();
        UnmanagedBean bean = factory.getInstance(UnmanagedBean.class);
        assertThat(bean.getClass(), is(UnmanagedBean.class));
        factory.stop();
    }

    static class OtherStepDefinitions {

    }

    static class StepDefinitions {

        @Inject
        OtherStepDefinitions injected;

    }

    @Test
    void shouldInjectStepDefinitions() {
        factory.addClass(OtherStepDefinitions.class);
        factory.addClass(StepDefinitions.class);
        factory.start();
        StepDefinitions stepDefinitions = factory.getInstance(StepDefinitions.class);
        assertThat(stepDefinitions.injected, is(notNullValue()));
        factory.stop();
    }

}
