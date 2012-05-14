package cucumber.runtime.java.weld;

import cucumber.runtime.java.ObjectFactory;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

public class WeldFactoryTest {
    @Test
    public void shouldGiveUsNewInstancesForEachScenario() {
        ObjectFactory factory = new WeldFactory();
        factory.addClass(BellyStepdefs.class);

        // Scenario 1
        factory.start();
        BellyStepdefs o1 = factory.getInstance(BellyStepdefs.class);
        factory.stop();

        // Scenario 2
        factory.start();
        BellyStepdefs o2 = factory.getInstance(BellyStepdefs.class);
        factory.stop();

        assertNotNull(o1);
        assertNotSame(o1, o2);
    }

}
