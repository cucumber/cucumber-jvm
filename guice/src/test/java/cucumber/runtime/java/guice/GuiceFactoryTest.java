package cucumber.runtime.java.guice;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import cucumber.runtime.java.ObjectFactory;

import java.io.IOException;
import java.util.Properties;

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

    @Test
    public void copeWithMissingGuiceModuleProperty() throws Exception {
        ObjectFactory factory = new GuiceFactory(new Properties());
        factory.createInstances();
        Mappings mappings = factory.getInstance(Mappings.class);
        assertThat(mappings, is(notNullValue()));
    }
}