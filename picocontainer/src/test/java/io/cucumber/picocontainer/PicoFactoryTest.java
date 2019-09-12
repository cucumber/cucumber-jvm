package io.cucumber.picocontainer;

import io.cucumber.core.backend.ObjectFactory;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PicoFactoryTest {

    @Test
    void shouldGiveUsNewInstancesForEachScenario() {
        ObjectFactory factory = new PicoFactory();
        factory.addClass(Steps.class);

        // Scenario 1
        factory.start();
        Steps o1 = factory.getInstance(Steps.class);
        factory.stop();

        // Scenario 2
        factory.start();
        Steps o2 = factory.getInstance(Steps.class);
        factory.stop();

        assertAll("Checking StepDefs",
            () -> assertThat(o1, is(notNullValue())),
            () -> assertThat(o1, is(not(equalTo(o2)))),
            () -> assertThat(o2, is(not(equalTo(o1))))
        );
    }

    @Test
    void shouldDisposeOnStop() {
        // Given
        ObjectFactory factory = new PicoFactory();
        factory.addClass(Steps.class);

        // When
        factory.start();
        Steps steps = factory.getInstance(Steps.class);

        // Then
        assertFalse(steps.getBelly().isDisposed());

        // When
        factory.stop();

        // Then
        assertTrue(steps.getBelly().isDisposed());
    }

}
