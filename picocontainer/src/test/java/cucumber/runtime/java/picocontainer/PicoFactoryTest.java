package cucumber.runtime.java.picocontainer;

import cucumber.runtime.java.ObjectFactory;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PicoFactoryTest {
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

    @Test
    public void shouldNotTryToInstantiateAnAbstractClass() {
        ObjectFactory factory = new PicoFactory();
        factory.addClass(AbstractStepDefs.class);

        factory.createInstances();
        AbstractStepDefs o = factory.getInstance(AbstractStepDefs.class);

        assertTrue("factory.createInstances must not crash", true);
        assertNull(o);
    }

}
