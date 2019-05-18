package io.cucumber.cdi2;

import io.cucumber.core.backend.ObjectFactory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

public class Cdi2FactoryTest {

    @Test
    public void shouldGiveUsNewInstancesForEachScenario() {

        final ObjectFactory factory = new Cdi2Factory();
        factory.addClass(BellyStepdefs.class);
        factory.addClass(CDIBellyStepdefs.class);

        // Scenario 1
        factory.start();
        final BellyStepdefs o1 = factory.getInstance(BellyStepdefs.class);
        final CDIBellyStepdefs cdiStep = factory.getInstance(CDIBellyStepdefs.class);
        assertNotEquals(CDIBellyStepdefs.class, cdiStep.getClass()); // it is a CDI proxy
        assertEquals(CDIBellyStepdefs.class, cdiStep.getClass().getSuperclass());
        factory.stop();

        // Scenario 2
        factory.start();
        final BellyStepdefs o2 = factory.getInstance(BellyStepdefs.class);
        factory.stop();

        assertNotNull(o1);
        assertNotSame(o1, o2);
    }
}
