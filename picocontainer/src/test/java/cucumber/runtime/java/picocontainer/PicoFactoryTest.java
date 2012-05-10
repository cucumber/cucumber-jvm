package cucumber.runtime.java.picocontainer;

import cucumber.runtime.java.ObjectFactory;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

public class PicoFactoryTest {
    @Test
    public void shouldGiveUsNewInstancesForEachScenario() {
        ObjectFactory factory = new PicoFactory();
        factory.addClass(StepDefs.class);

        // Scenario 1
        factory.start();
        StepDefs o1 = factory.getInstance(StepDefs.class);
        factory.stop();

        // Scenario 2
        factory.start();
        StepDefs o2 = factory.getInstance(StepDefs.class);
        factory.stop();

        assertNotNull(o1);
        assertNotSame(o1, o2);
    }

}
