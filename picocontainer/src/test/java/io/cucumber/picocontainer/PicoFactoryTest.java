package io.cucumber.picocontainer;

import io.cucumber.core.backend.ObjectFactory;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PicoFactoryTest {

    @Test
    void shouldGiveUsNewInstancesForEachScenario() {
        ObjectFactory factory = new PicoFactory();
        factory.addClass(StepDefinitions.class);

        // Scenario 1
        factory.start();
        StepDefinitions o1 = factory.getInstance(StepDefinitions.class);
        factory.stop();

        // Scenario 2
        factory.start();
        StepDefinitions o2 = factory.getInstance(StepDefinitions.class);
        factory.stop();

        assertAll(
            () -> assertThat(o1, is(notNullValue())),
            () -> assertThat(o1, is(not(equalTo(o2)))),
            () -> assertThat(o2, is(not(equalTo(o1)))));
    }

    @Test
    void shouldDisposeOnStop() {
        // Given
        ObjectFactory factory = new PicoFactory();
        factory.addClass(StepDefinitions.class);

        // When
        factory.start();
        StepDefinitions steps = factory.getInstance(StepDefinitions.class);

        // Then
        assertFalse(steps.getBelly().isDisposed());

        // When
        factory.stop();

        // Then
        assertTrue(steps.getBelly().isDisposed());
    }

    @Test
    void public_non_static_inner_classes_are_not_instantiable() {
        ObjectFactory factory = new PicoFactory();
        factory.addClass(NonStaticInnerClass.class);
        factory.start();

        assertThat(factory.getInstance(NonStaticInnerClass.class), nullValue());
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    public class NonStaticInnerClass {

    }

}
