package cucumber.runtime.java.guice;

import cucumber.runtime.java.ObjectFactory;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.*;

public class GuiceFactoryTest {
    @Test
    public void shouldGiveUsNewInstancesForEachScenario() throws IOException {
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

    // TODO: verify that this is what the author meant
    @Test
    public void missing_guice_module_property_causes_mapping_to_be_null() throws Exception {
        ObjectFactory factory = new GuiceFactory(new Properties());
        factory.createInstances();
        Mappings mappings = factory.getInstance(Mappings.class);
        assertNull(mappings);
    }
}