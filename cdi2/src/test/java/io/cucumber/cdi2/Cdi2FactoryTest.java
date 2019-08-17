package io.cucumber.cdi2;

import io.cucumber.core.backend.ObjectFactory;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.Is.isA;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertAll;

class Cdi2FactoryTest {

    @Test
    void shouldGiveUsNewInstancesForEachScenario() {

        final ObjectFactory factory = new Cdi2Factory();
        factory.addClass(BellyStepdefs.class);
        factory.addClass(CDIBellyStepdefs.class);

        // Scenario 1
        factory.start();
        final BellyStepdefs o1 = factory.getInstance(BellyStepdefs.class);
        final CDIBellyStepdefs cdiStep = factory.getInstance(CDIBellyStepdefs.class);
        assertAll("Checking CDIBellyStepdefs",
            () -> assertThat(cdiStep.getClass(), not(isA(CDIBellyStepdefs.class))), // it is a CDI proxy
            () -> assertThat(cdiStep.getClass().getSuperclass(), isA(CDIBellyStepdefs.class))
        );
        factory.stop();

        // Scenario 2
        factory.start();
        final BellyStepdefs o2 = factory.getInstance(BellyStepdefs.class);
        factory.stop();

        assertAll("Checking BellyStepdefs",
            () -> assertThat(o1, is(notNullValue())),
            () -> assertThat(o1, is(not(equalTo(o2)))),
            () -> assertThat(o2, is(not(equalTo(o1))))
        );
    }

}
