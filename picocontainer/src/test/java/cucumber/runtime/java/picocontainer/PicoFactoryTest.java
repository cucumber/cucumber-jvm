package cucumber.runtime.java.picocontainer;

import cucumber.runtime.java.ObjectFactory;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

public class PicoFactoryTest {
    @Ignore("Needs to be fixed")
    @Test
    public void shouldGiveUsNewInstancesForEachScenario() {
        ObjectFactory factory = new PicoFactory();
        factory.addClass(StepDefs.class);

        // Scenario 1
        factory.createInstances();
        StepDefs o1 = factory.getInstance(StepDefs.class);
        factory.disposeInstances();

        // Scenario 2
        factory.createInstances();
        StepDefs o2 = factory.getInstance(StepDefs.class);
        factory.disposeInstances();

        assertNotNull(o1);
        assertNotSame(o1, o2);
    }

}
