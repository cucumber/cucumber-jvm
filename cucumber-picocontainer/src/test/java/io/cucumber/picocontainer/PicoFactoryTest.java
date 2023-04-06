package io.cucumber.picocontainer;

import io.cucumber.core.backend.ObjectFactory;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
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
            () -> assertNotNull(o1),
            () -> assertNotSame(o1, o2));
    }

    @Test
    void shouldCreateNewTransitiveDependencies() {
        ObjectFactory factory = new PicoFactory();
        factory.addClass(StepDefinitionsWithTransitiveDependencies.class);

        // Scenario 1
        factory.start();
        StepDefinitionsWithTransitiveDependencies o1 = factory
                .getInstance(StepDefinitionsWithTransitiveDependencies.class);
        factory.stop();

        // Scenario 2
        factory.start();
        StepDefinitionsWithTransitiveDependencies o2 = factory
                .getInstance(StepDefinitionsWithTransitiveDependencies.class);
        factory.stop();

        assertAll(
            () -> assertNotSame(o1.firstDependency, o2.firstDependency),
            () -> assertNotSame(o1.firstDependency.secondDependency, o2.firstDependency.secondDependency));
    }

    @Test
    void shouldInvokeLifeCycleMethods() {
        // Given
        ObjectFactory factory = new PicoFactory();
        factory.addClass(StepDefinitions.class);

        // When
        factory.start();
        StepDefinitions steps = factory.getInstance(StepDefinitions.class);

        // Then
        assertTrue(steps.getBelly().wasStarted());
        assertFalse(steps.getBelly().wasStopped());
        assertFalse(steps.getBelly().isDisposed());

        // When
        factory.stop();

        // Then
        assertTrue(steps.getBelly().wasStarted());
        assertTrue(steps.getBelly().wasStopped());
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
