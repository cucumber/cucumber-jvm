package cucumber.runtime.java.guice;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import cucumber.runtime.java.ObjectFactory;

public class GuiceFactoryTest {
    private final ObjectFactory factory = new GuiceFactory();

    @Before
    public void registerStepsClassWithFactory() {
        factory.addClass(Mappings.class);
    }

    @Test
    public void shouldGiveUsNewInstancesForEachScenario() {
        // Scenario 1
        factory.createInstances();
        Mappings o1 = instantiateStepsClass();
        factory.disposeInstances();

        // Scenario 2
        factory.createInstances();
        Mappings o2 = instantiateStepsClass();
        factory.disposeInstances();

        assertNotNull(o1);
        assertNotSame(o1, o2);
    }

    @Test
    public void copeWithMissingGuiceModuleProperty() throws Exception {
        factory.createInstances();
        assertThat(instantiateStepsClass(), is(notNullValue()));
    }
    
    private Mappings instantiateStepsClass() {
        return factory.getInstance(Mappings.class);
    }
}