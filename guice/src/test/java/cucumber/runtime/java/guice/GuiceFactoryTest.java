package cucumber.runtime.java.guice;

import cucumber.runtime.java.ObjectFactory;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

public class GuiceFactoryTest {
    @Ignore("Needs to be fixed")
    @Test
    public void shouldGiveUsNewInstancesForEachScenario() {
        ObjectFactory factory = new GuiceFactory();
        factory.addClass(Mappings.class);
        
        // Scenario 1
        factory.createInstances();
        Mappings o1 = factory.getInstance(Mappings.class);
        factory.disposeInstances();

        // Scenario 2
        factory.createInstances();
        Mappings o2 = factory.getInstance(Mappings.class);
        factory.disposeInstances();
        
        assertNotNull(o1);
        assertNotSame(o1, o2);
    }

}
