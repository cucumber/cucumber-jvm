package io.cucumber.cdi2;

import io.cucumber.core.backend.ObjectFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class Cdi2FactoryTest {

    final ObjectFactory factory = new Cdi2Factory();

    @Test
    void shouldCreateNewInstancesForEachScenario() {
        factory.addClass(BellyStepDefinitions.class);

        // Scenario 1
        factory.start();
        BellyStepDefinitions a1 = factory.getInstance(BellyStepDefinitions.class);
        BellyStepDefinitions a2 = factory.getInstance(BellyStepDefinitions.class);
         assertThat(a1, is(equalTo(a2)));
        factory.stop();

        // Scenario 2
        factory.start();
        BellyStepDefinitions b1 = factory.getInstance(BellyStepDefinitions.class);
        factory.stop();

        assertAll(
            () -> assertThat(a1, is(notNullValue())),
            () -> assertThat(a1, is(not(equalTo(b1)))),
            () -> assertThat(b1, is(not(equalTo(a1)))));
    }

    @Test
    void shouldCreateApplicationScopedInstance() {
        factory.addClass(CdiBellyStepDefinitions.class);
        factory.start();
        CdiBellyStepDefinitions cdiStep = factory.getInstance(CdiBellyStepDefinitions.class);
        assertAll(
                // assert that it is is a CDI proxy
                () -> assertThat(cdiStep.getClass(), not(is(CdiBellyStepDefinitions.class))),
                () -> assertThat(cdiStep.getClass().getSuperclass(), is(CdiBellyStepDefinitions.class)));
        factory.stop();
    }

    @Test
    void shouldCreateUnmanagedInstance() {
        factory.addClass(UnmanagedBellyStepDefinitions.class);
        factory.start();
        assertNotNull(factory.getInstance(UnmanagedBellyStepDefinitions.class));
        factory.stop();
    }

}
