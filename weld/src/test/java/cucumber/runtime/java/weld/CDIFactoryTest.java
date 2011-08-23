package cucumber.runtime.java.weld;

import cucumber.runtime.java.ObjectFactory;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

public class CDIFactoryTest {
    @Test
    public void shouldGiveUsNewInstancesForEachScenario() {
        ObjectFactory factory = new CDIFactory();
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
