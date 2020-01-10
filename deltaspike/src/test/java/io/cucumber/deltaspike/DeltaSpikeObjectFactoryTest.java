package io.cucumber.deltaspike;

import io.cucumber.core.backend.ObjectFactory;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

public class DeltaSpikeObjectFactoryTest {

    private final ObjectFactory factory = new DeltaSpikeObjectFactory();

    @Test
    public void shouldGiveUsNewInstancesForEachScenario() {
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
