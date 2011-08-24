package cucumber.runtime.java.spring;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import org.junit.Test;

import cucumber.runtime.java.ObjectFactory;

public class SpringFactoryTest {
    // @Ignore("Needs to be fixed")
    @Test
    public void shouldGiveUsNewInstancesForEachScenario() {
        final ObjectFactory factory = new SpringFactory();
        factory.addClass(BellyStepdefs.class);

        // Scenario 1
        factory.createInstances();
        final BellyStepdefs o1 = factory.getInstance(BellyStepdefs.class);
        factory.disposeInstances();

        // Scenario 2
        factory.createInstances();
        final BellyStepdefs o2 = factory.getInstance(BellyStepdefs.class);
        factory.disposeInstances();

        assertNotNull(o1);
        assertNotNull(o2);
        assertNotSame(o1, o2);
    }

}
