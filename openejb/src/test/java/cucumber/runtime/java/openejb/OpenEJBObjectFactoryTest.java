package cucumber.runtime.java.openejb;

import cucumber.runtime.java.ObjectFactory;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

public class OpenEJBObjectFactoryTest {
    @Test
    public void shouldGiveUsNewInstancesForEachScenario() {
        ObjectFactory factory = new OpenEJBObjectFactory();
        factory.addClass(BellyStepdefs.class);

        // Scenario 1
        factory.createInstances();
        BellyStepdefs o1 = factory.getInstance(BellyStepdefs.class);
        factory.disposeInstances();

        // Scenario 2
        factory.createInstances();
        BellyStepdefs o2 = factory.getInstance(BellyStepdefs.class);
        factory.disposeInstances();

        assertNotNull(o1);
        assertNotSame(o1, o2);
    }

}
