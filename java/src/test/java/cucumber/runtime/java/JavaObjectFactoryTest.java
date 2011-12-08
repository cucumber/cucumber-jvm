package cucumber.runtime.java;

import cucumber.fallback.runtime.java.DefaultJavaObjectFactory;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

public class JavaObjectFactoryTest {
    @Test
    public void shouldGiveUsNewInstancesForEachScenario() {
        ObjectFactory factory = new DefaultJavaObjectFactory();
        factory.addClass(DriveSteDef.class);

        // Scenario 1
        factory.createInstances();
        DriveSteDef o1 = factory.getInstance(DriveSteDef.class);
        factory.disposeInstances();

        // Scenario 2
        factory.createInstances();
        DriveSteDef o2 = factory.getInstance(DriveSteDef.class);
        factory.disposeInstances();

        assertNotNull(o1);
        assertNotSame(o1, o2);
    }
}
