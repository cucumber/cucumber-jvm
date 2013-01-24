package cucumber.runtime.java.picocontainer;

import java.io.IOException;
import java.util.Properties;

import cucumber.runtime.java.picocontainer.configuration.NoOpPicoConfigurer;
import cucumber.runtime.java.picocontainer.configuration.PicoConfigurer;
import cucumber.runtime.java.picocontainer.configuration.PicoConfigurerInstantiator;

public class CustomPicoConfigurerFactory {

    private PropertyLoader propertyLoader;

    public CustomPicoConfigurerFactory(PropertyLoader propertyLoader) {
        this.propertyLoader = propertyLoader;
    }

    public PicoConfigurer getConfigurer() throws IOException {
        Properties properties = propertyLoader.getProperties();
        String picoConfigurerClassName = properties.getProperty("picoConfigurer");
        if (picoConfigurerClassName == null) {
            return new NoOpPicoConfigurer();
        }
        PicoConfigurer delegateConfigurer = new PicoConfigurerInstantiator().instantiate(picoConfigurerClassName);
        return new RunOncePicoConfigurer(delegateConfigurer);
    }

}
