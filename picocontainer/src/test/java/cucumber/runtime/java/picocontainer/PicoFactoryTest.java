package cucumber.runtime.java.picocontainer;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;

import java.util.Properties;

import org.junit.Test;

import cucumber.runtime.java.ObjectFactory;
import cucumber.runtime.java.picocontainer.configuration.CallCountingConfigurer;
import cucumber.runtime.java.picocontainer.configuration.GreeterImplementation;
import cucumber.runtime.java.picocontainer.configuration.GreeterInterface;
import cucumber.runtime.java.picocontainer.configuration.GreeterWithCollaborators;

public class PicoFactoryTest {
    @Test
    public void shouldGiveUsNewInstancesForEachScenario() throws Exception {
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
    public void shouldIgnoreInterfacesAsTheyCanNotBeInstantiated() throws Exception {
        ObjectFactory factory = new PicoFactory();
        factory.addClass(MyInterface.class);
        factory.start();
    }

    interface MyInterface {
    }

    @Test
    public void shouldAllowConfigurationOfThePicoInstance() throws Exception {
        ObjectFactory factory = aFactoryConfiguredWith("cucumber.runtime.java.picocontainer.configuration.YourPicoConfigurer");
        factory.start();

        assertThat(factory.getInstance(GreeterInterface.class),
                is(GreeterImplementation.class));
    }
    
    @Test
    public void shoudlOnlyRunTheCustomPicoConfigurerOnce() throws Exception {
        ObjectFactory factory = aFactoryConfiguredWith("cucumber.runtime.java.picocontainer.configuration.CallCountingConfigurer");
        factory.start();
        factory.stop();
        factory.start();

        assertThat(CallCountingConfigurer.timesRun, is(1));
    }
    
    @Test
    public void shouldAutowireDependenciesOfCustomConfiguredClass() throws Exception {
        ObjectFactory factory = aFactoryConfiguredWith("cucumber.runtime.java.picocontainer.configuration.ConfiguredClassHasDepdendenciesConfigurer");
        factory.start();

        assertThat(factory.getInstance(GreeterInterface.class),
                is(GreeterWithCollaborators.class));
    }

    private ObjectFactory aFactoryConfiguredWith(String configurer) {
        Properties properties = new Properties();
        properties.setProperty("picoConfigurer", configurer);

        ObjectFactory factory = new PicoFactory(properties);
        return factory;
    }

}
