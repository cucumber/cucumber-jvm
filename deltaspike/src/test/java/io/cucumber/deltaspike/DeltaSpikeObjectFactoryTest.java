package io.cucumber.deltaspike;

import io.cucumber.core.backend.ObjectFactory;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

public class DeltaSpikeObjectFactoryTest {

    private final ObjectFactory factory = new DeltaSpikeObjectFactory();

    @Test
    public void shouldGiveUsNewInstancesForEachScenario() {
        factory.addClass(BellyStepdefs.class);

        // Scenario 1
        factory.start();
        final BellyStepdefs o1 = factory.getInstance(BellyStepdefs.class);
        factory.stop();

        // Scenario 2
        factory.start();
        final BellyStepdefs o2 = factory.getInstance(BellyStepdefs.class);
        factory.stop();

        assertNotNull(o1);
        assertNotSame(o1, o2);
    }

}
