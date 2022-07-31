package io.cucumber.deltaspike;

import io.cucumber.core.backend.ObjectFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class DeltaSpikeObjectFactoryTest {

    private final ObjectFactory factory = new DeltaSpikeObjectFactory();

    @Test
    void shouldGiveUsNewInstancesForEachScenario() {
        factory.addClass(BellyStepDefinitions.class);

        // Scenario 1
        factory.start();
        final BellyStepDefinitions o1 = factory.getInstance(BellyStepDefinitions.class);
        factory.stop();

        // Scenario 2
        factory.start();
        final BellyStepDefinitions o2 = factory.getInstance(BellyStepDefinitions.class);
        factory.stop();

        assertNotNull(o1);
        assertNotSame(o1, o2);
    }

}
