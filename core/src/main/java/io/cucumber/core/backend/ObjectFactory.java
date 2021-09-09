package io.cucumber.core.backend;

import org.apiguardian.api.API;

/**
 * Instantiates glue classes. Loaded via SPI.
 * <p>
 * Cucumber scenarios are executed against a test context that consists of
 * multiple glue classes. These must be instantiated and may optionally be
 * injected with dependencies.
 * <p>
 * When multiple {@code ObjectFactory} implementations are available Cucumber
 * will look for a preference in the provided properties or options.
 *
 * @see java.util.ServiceLoader
 * @see io.cucumber.core.runtime.ObjectFactoryServiceLoader
 */
@API(status = API.Status.STABLE)
public interface ObjectFactory extends Container, Lookup {

    /**
     * Start the object factory. Invoked once per scenario.
     * <p>
     * While started {@link Lookup#getInstance(Class)} may be invoked.
     */
    void start();

    /**
     * Stops the object factory. Called once per scenario.
     * <p>
     * When stopped the object factory should dispose of all glue instances.
     */
    void stop();

}
