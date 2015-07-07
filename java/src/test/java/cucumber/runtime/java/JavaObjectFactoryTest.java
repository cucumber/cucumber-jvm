package cucumber.runtime.java;

import cucumber.api.java.ObjectFactory;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

public class JavaObjectFactoryTest {
    @Test
    public void shouldGiveUsNewInstancesForEachScenario() {
        ObjectFactory factory = new DefaultJavaObjectFactory();
        factory.addClass(SteDef.class);

        // Scenario 1
        factory.start();
        SteDef o1 = factory.getInstance(SteDef.class);
        factory.stop();

        // Scenario 2
        factory.start();
        SteDef o2 = factory.getInstance(SteDef.class);
        factory.stop();

        assertNotNull(o1);
        assertNotSame(o1, o2);
    }

    public static class SteDef {
        // we just test the instances
    }
}
