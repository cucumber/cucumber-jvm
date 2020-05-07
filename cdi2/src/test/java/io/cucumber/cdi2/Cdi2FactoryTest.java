package io.cucumber.cdi2;

import io.cucumber.core.backend.ObjectFactory;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertAll;

class Cdi2FactoryTest {

    @Test
    void shouldGiveUsNewInstancesForEachScenario() {

        final ObjectFactory factory = new Cdi2Factory();
        factory.addClass(BellyStepDefinitions.class);
        factory.addClass(CdiBellyStepDefinitions.class);

        // Scenario 1
        factory.start();
        final BellyStepDefinitions o1 = factory.getInstance(BellyStepDefinitions.class);
        final CdiBellyStepDefinitions cdiStep = factory.getInstance(CdiBellyStepDefinitions.class);
        assertAll(
            // assert that it is is a CDI proxy
            () -> assertThat(cdiStep.getClass(), not(is(CdiBellyStepDefinitions.class))),
            () -> assertThat(cdiStep.getClass().getSuperclass(), is(CdiBellyStepDefinitions.class)));
        factory.stop();

        // Scenario 2
        factory.start();
        final BellyStepDefinitions o2 = factory.getInstance(BellyStepDefinitions.class);
        factory.stop();

        assertAll(
            () -> assertThat(o1, is(notNullValue())),
            () -> assertThat(o1, is(not(equalTo(o2)))),
            () -> assertThat(o2, is(not(equalTo(o1)))));
    }

}
