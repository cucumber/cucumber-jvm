package cucumber.runtime.java.picocontainer;

import io.cucumber.core.backend.ObjectFactory;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void shouldDisposeOnStop() {
        // Given
        ObjectFactory factory = new PicoFactory();
        factory.addClass(StepDefs.class);

        // When
        factory.start();
        StepDefs steps = factory.getInstance(StepDefs.class);

        // Then
        assertFalse(steps.getBelly().isDisposed());

        // When
        factory.stop();

        // Then
        assertTrue(steps.getBelly().isDisposed());
    }
}
